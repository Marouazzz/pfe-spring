package org.sid.pfespring.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.sid.pfespring.dto.RequestPFEDTO;
import org.sid.pfespring.dto.ResponsePFEDTO;
import org.sid.pfespring.mapper.PFEMapper;
import org.sid.pfespring.model.Etudiant;
import org.sid.pfespring.model.PFE;
import org.sid.pfespring.model.Prof;
import org.sid.pfespring.model.Specialite;
import org.sid.pfespring.repository.EtudiantRepository;
import org.sid.pfespring.repository.PFERepository;
import org.sid.pfespring.repository.ProfRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;



@Service
@Validated // We use this => In order to validate entities from the service bean 
public class PFEServiceImpl extends AbstractService<PFE, RequestPFEDTO, ResponsePFEDTO> implements PFEService {

    private EtudiantRepository etudrepo;
    private ProfRepository profrepo;
    private Validator validator;




    public PFEServiceImpl(PFERepository repository,PFEMapper mapper,EtudiantRepository etudiantRepository,ProfRepository profRepository,Validator validator){
        super(repository,mapper);
        this.etudrepo = etudiantRepository;
        this.profrepo = profRepository;
        this.validator = validator;

    }

    @Override
    public int affecterProfPFE() {
        List<Specialite> langues  = List.of(Specialite.ANGLAIS,Specialite.FRANCAIS);
        List<Prof> profs = profrepo.findBySpecialiteNotIn(langues);
        List<PFE> pfes = repository.findAll();


        // Compter prof par etudiant 
        if(pfes!=null && profs !=null && !pfes.isEmpty() && !profs.isEmpty()){
            // Apply a shuffle 
            Collections.shuffle(profs);
            Collections.shuffle(pfes);

            if(pfes.size() < profs.size()){
                // realiser un chuffle etudiant par prof / ou bien declencher un erreur 
                // Will be done later after discussion 
                for(int i=0; i < pfes.size() ; i++){
                    PFE pfe = pfes.get(i);
                    Prof prof = profs.get(i);
                    pfe.setProf(prof);
                }
            }else{
                // Calculer combien des etudiant affecter a un prof
                int capacite_minimum = pfes.size() / profs.size();
                // Combien des etudiants restant
                int reste_etud = pfes.size() % profs.size();
                int indexPfe = 0;
                for (Prof prof : profs){
                    int count = capacite_minimum + (reste_etud > 0 ? 1:0 );
                    for(int i=0; i < count ;i++){
                        PFE pfe = pfes.get(indexPfe);
                        pfe.setProf(prof);
                        indexPfe++;
                    }
                    reste_etud--;
                }
            }
            repository.saveAll(pfes);
            return pfes.size();
        }else{
                // Throw errors based on unsatisfied condition
                // To be updated
                return 0;
            }

    }

    @Override
    public List<ResponsePFEDTO> importFromExcel(MultipartFile file) {
        // Don't forget to handle exception !!!!!!!!!!!!!
        List<RequestPFEDTO> sujetPfedtos = readExcel(file);
        List<String> missingCnes = ValidateEtudiants(sujetPfedtos);

        if(missingCnes.isEmpty()){
            List<String> cnes = sujetPfedtos.stream()
            .map(RequestPFEDTO::cne)
            .toList();

            List<Etudiant> etudiants = etudrepo.findByCneIn(cnes);
            Map<String,Etudiant> etudiantsMap = new HashMap<>();

            for(Etudiant etudiant : etudiants){
                etudiantsMap.put(etudiant.getCne(), etudiant);
            }
            etudiants.clear();
            List<PFE> pfes = new ArrayList<>();
            for(RequestPFEDTO rpfedto : sujetPfedtos){
                        PFE pfe = mapper.toEntity(rpfedto);
                        pfe.setEtudiant(etudiantsMap.get(rpfedto.cne()));
                        pfes.add(pfe);
            }
            repository.saveAll(pfes);
        return pfes.stream().map(mapper::toResponse).toList();
        }   
        // this will be discussed later
        return Collections.emptyList();
    }

    private List<RequestPFEDTO> readExcel(MultipartFile file){
        try(Workbook workbook = WorkbookFactory.create(file.getInputStream())){
            // Validation Exception will be handled later 
            List<RequestPFEDTO> sujetsPfe = new ArrayList<>();
            Sheet sheet = workbook.getSheet("pfes");
            DataFormatter formater = new DataFormatter();
            // The last row is uncluded
            for(int i =1; i <= sheet.getLastRowNum();i++){
                Row row = sheet.getRow(i);
                if(row == null) continue;
                String cne = formater.formatCellValue(row.getCell(0)).trim();
                String sujet = formater.formatCellValue(row.getCell(1)).trim();
                String description = formater.formatCellValue(row.getCell(2)).trim();
                RequestPFEDTO pfedto = new RequestPFEDTO(cne, sujet, description);

                Set<ConstraintViolation<RequestPFEDTO>> violations = validator.validate(pfedto);
                if(!violations.isEmpty()){
                    ConstraintViolation<RequestPFEDTO> firstViolation = violations.iterator().next();
                    throw new RuntimeException("Fichier erroné à la ligne " + (i + 1) + " : " + firstViolation.getMessage()); 
                }
                sujetsPfe.add(pfedto);
            }

            return sujetsPfe;
        }catch(IOException e){
            // Handle this login later 
            e.printStackTrace();
            return null;
        }
    }

    private List<String> ValidateEtudiants(List<RequestPFEDTO> sujetPFEs){
            List<String> excelCnes = sujetPFEs.stream()
            .map(RequestPFEDTO::cne)
            // toList() : returns immutable list 
            // Use collect() : 
            .collect(Collectors.toCollection(ArrayList::new));

            List<String> existingCnes = etudrepo.findExistingCNE(excelCnes);

            excelCnes.removeAll(existingCnes);

            return excelCnes;
        }
    }