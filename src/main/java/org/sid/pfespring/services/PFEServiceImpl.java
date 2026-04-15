package org.sid.pfespring.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.sid.pfespring.dto.RequestPFEDTO;
import org.sid.pfespring.dto.ResponsePFEDTO;
import org.sid.pfespring.mapper.PFEMapper;
import org.sid.pfespring.model.Encadrant;
import org.sid.pfespring.model.Etudiant;
import org.sid.pfespring.model.PFE;
import org.sid.pfespring.model.Prof;
import org.sid.pfespring.model.Specialite;
import org.sid.pfespring.repository.EtudiantRepository;
import org.sid.pfespring.repository.PFERepository;
import org.sid.pfespring.repository.ProfRepository;
import org.sid.pfespring.repository.EncadrantRepository;
import org.sid.pfespring.utils.ExcelGenerator;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;



@Service
@Validated // We use this => In order to validate entities from the any bean rather than @Controller 
public class PFEServiceImpl extends AbstractService<PFE, RequestPFEDTO, ResponsePFEDTO> implements PFEService {

    private EtudiantRepository etudrepo;
    private ProfRepository profrepo;
    private EncadrantRepository encadrantrepo;
    private Validator validator;
    private ExcelGenerator excelgenerator;




    public PFEServiceImpl(PFERepository repository,PFEMapper mapper,EtudiantRepository etudiantRepository,ProfRepository profRepository,EncadrantRepository encadrantRepository,Validator validator,ExcelGenerator excelGenerator){
        super(repository,mapper);
        this.etudrepo = etudiantRepository;
        this.profrepo = profRepository;
        this.encadrantrepo = encadrantRepository;
        this.validator = validator;
        this.excelgenerator = excelGenerator;

    }

    // @Override
    public int affecterProfPFE() {
/*         List<Specialite> langues  = List.of(Specialite.ANGLAIS,Specialite.FRANCAIS);
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
                    pfe.setEncadrant(prof);
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
            } */
        return 0;
    }

    // @Override
    public List<ResponsePFEDTO> importFromExcel(MultipartFile file) {
/*         // Don't forget to handle exception !!!!!!!!!!!!!
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
        // this will be discussed later */
        return Collections.emptyList(); 
    }

    @Override
    public List<RequestPFEDTO> readExcel(MultipartFile file){
        try(Workbook workbook = WorkbookFactory.create(file.getInputStream())){
            // Validation Exception will be handled later 
            List<RequestPFEDTO> sujetsPfe = new ArrayList<>();
            Sheet sheet = workbook.getSheet("pfe_v2");
            DataFormatter formater = new DataFormatter();
            // The last row is uncluded
            for(int i =1; i <= sheet.getLastRowNum();i++){
                Row row = sheet.getRow(i);
                if(row == null) continue;
                String sujet = formater.formatCellValue(row.getCell(0)).trim();
                String rawCnes = formater.formatCellValue(row.getCell(1));
                Set<String> cnes = Arrays.stream(rawCnes.split(","))
                    .map(String::trim)
                    .map(s -> s.replace("\u00A0", ""))
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());
                String description = formater.formatCellValue(row.getCell(2)).trim();
                RequestPFEDTO pfedto = new RequestPFEDTO(cnes, sujet, description);

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

    private List<String> ValidateEtudiants(List<RequestPFEDTO> sujetPFEs,List<String> etudiants){
            List<String> excelCnes = sujetPFEs.stream()
            .flatMap(dto -> dto.cnes().stream())
            // toList() : returns immutable list 
            // Use collect() : 
            .collect(Collectors.toCollection(ArrayList::new));
            excelCnes.removeAll(etudiants);

            return excelCnes;
        }

@Override
public void appliquerAffectation(List<RequestPFEDTO> pfesDto) {

    List<Etudiant> etudiants = etudrepo.findAll();
    Map<String, Etudiant> etudiantMap = etudiants.stream()
            .collect(Collectors.toMap(Etudiant::getCne, e -> e));
    List<String> cnes = etudiantMap.keySet().stream().toList();

    List<String> anomalies = ValidateEtudiants(pfesDto,cnes);
    if (!anomalies.isEmpty()) {
        throw new RuntimeException("CNEs introuvables: " + anomalies);
    }
    List<Specialite> langues = List.of(Specialite.ANGLAIS, Specialite.FRANCAIS);
    List<Prof> profs = profrepo.findBySpecialiteNotIn(langues);

    if (profs.isEmpty()) {
        throw new RuntimeException("Aucun prof disponible");
    }

    // Convert prof to encadrant
    List<Encadrant> encadrants = profs.stream()
            .map(prof -> Encadrant.builder()
                    .prof(prof)
                    .pfes(new ArrayList<>())
                    .build())
            .collect(Collectors.toList());

    //Convert dto to pwfe 
    List<PFE> pfes = new ArrayList<>();
    
    System.out.println(etudiantMap.size());
    for (RequestPFEDTO dto : pfesDto) {
        PFE pfe = mapper.toEntity(dto);
        Set<Etudiant> etuds = dto.cnes().stream()
        .map(etudiantMap::get)
        .collect(Collectors.toSet());
        pfe.setEtudiants(etuds);
        pfes.add(pfe);
    }

    // Apply shuffle
    Collections.shuffle(encadrants);
    Collections.shuffle(pfes);

    // calcule la capacite
    int capaciteMin = pfes.size() / encadrants.size();
    int reste = pfes.size() % encadrants.size();

    int index = 0;

    for (Encadrant encadrant : encadrants) {

        int count = capaciteMin + (reste > 0 ? 1 : 0);

        for (int i = 0; i < count && index < pfes.size(); i++) {
            PFE pfe = pfes.get(index);
            pfe.setEncadrant(encadrant);
            encadrant.getPfes().add(pfe);
            index++;
        }

        reste--;
    }

    // 8. Sauvegarde (important)
    encadrantrepo.saveAll(encadrants);
    repository.saveAll(pfes);
    etudiants = new ArrayList<>();
    for (PFE pfe :pfes){
        Set<Etudiant> etuds = pfe.getEtudiants();
        for (Etudiant e :etuds){
            e.setPfe(pfe);
            etudiants.add(e);
        }
    }
    etudrepo.saveAll(etudiants);
}
  @Override
  public byte[] exportPFEAffectation() throws IOException {
    List<Encadrant> encdrant = encadrantrepo.findAll();
/*     for (Encadrant enc:encdrant){
        for (PFE p : enc.getPfes()) {
            System.out.println("PFE " + p.getId());
            for (Etudiant e : p.getEtudiants()) {
                System.out.println(" - " + e);
            }
        }
    } */
        Map<String, Map<Long, String>> affectations = encdrant.stream()
    .collect(
        Collectors.toMap(
            e->e.getProf().toString(),
            e -> e.getPfes().stream()
                            .collect(
                                Collectors.toMap(
                                    PFE::getId,
                                    pfe -> pfe.getEtudiants().stream()
                                                             .map(Etudiant::toString)
                                                             .collect(Collectors.joining(", "))
                                                ) 
                                    )
                        )
        );
        return excelgenerator.exportPFEAffectationSheet(affectations);
  }

   
}
