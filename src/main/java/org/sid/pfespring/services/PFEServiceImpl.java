package org.sid.pfespring.services;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.sid.pfespring.dto.RequestPFEDTO;
import org.sid.pfespring.dto.ResponsePFEDTO;
import org.sid.pfespring.exception.BusinessException;
import org.sid.pfespring.exception.EtudiantNotFoundException;
import org.sid.pfespring.exception.PFEImportValidationException;
import org.sid.pfespring.mapper.PFEMapper;
import org.sid.pfespring.model.Encadrant;
import org.sid.pfespring.model.Etudiant;
import org.sid.pfespring.model.Filiere;
import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.model.Langue;
import org.sid.pfespring.model.PFE;
import org.sid.pfespring.model.Prof;
import org.sid.pfespring.model.Specialite;
import org.sid.pfespring.model.Status;
import org.sid.pfespring.repository.EncadrantRepository;
import org.sid.pfespring.repository.EtudiantRepository;
import org.sid.pfespring.repository.ImportVersionRepository;
import org.sid.pfespring.repository.PFERepository;
import org.sid.pfespring.repository.ProfRepository;
import org.sid.pfespring.utils.ExcelGenerator;
import org.sid.pfespring.utils.PDFGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;



@Service
@Validated // We use this => In order to validate entities from the any bean rather than @Controller
public class PFEServiceImpl extends AbstractService<PFE, RequestPFEDTO, ResponsePFEDTO> implements PFEService {

    private EtudiantRepository etudrepo;
    private ProfRepository profrepo;
    private EncadrantRepository encadrantrepo;
    private ImportVersionRepository versionrepo;
    private FileSystemService fsService;
    private Validator validator;




    public PFEServiceImpl(PFERepository repository,PFEMapper mapper,EtudiantRepository etudiantRepository,ProfRepository profRepository,EncadrantRepository encadrantRepository,ImportVersionRepository versionRepository,FileSystemService fSystemService,Validator validator){
        super(repository,mapper);
        this.etudrepo = etudiantRepository;
        this.profrepo = profRepository;
        this.encadrantrepo = encadrantRepository;
        this.versionrepo = versionRepository;
        this.fsService = fSystemService;
        this.validator = validator;
    }

    @Transactional
    @Override
    public void importFromExcel(Sheet sheet,ImportVersion version) {
        List<RequestPFEDTO> pfedtos = readExcel(sheet);
        List<Etudiant> etudiants = etudrepo.findByVersion(version);
        Map<String, Etudiant> etudiantMap = etudiants.stream()
                .collect(Collectors.toMap(Etudiant::getCne, e -> e));
        List<String> cnes = etudiantMap.keySet().stream().toList();
        Set<String> anomalies = ValidateEtudiants(pfedtos,cnes);

        if (!anomalies.isEmpty())
            throw new EtudiantNotFoundException("Les etudiants ayant les CNE's suivants sont introuvables : " + anomalies);

        //Convert dto to pwfe
        List<PFE> pfes = new ArrayList<>();

        for (RequestPFEDTO dto : pfedtos) {
            PFE pfe = mapper.toEntity(dto);
            pfe.setVersion(version);
            Set<Etudiant> etuds = dto.cnes().stream()
                    .map(etudiantMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            for (Etudiant e :etuds){
                e.setPfe(pfe);
            }

            pfe.setEtudiants(etuds);
            pfes.add(pfe);
        }
        repository.saveAll(pfes);
        etudrepo.saveAll(etudiants);
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            if (cell != null) {
                String value = formatter.formatCellValue(cell).trim();
                if (!value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private List<RequestPFEDTO> readExcel(Sheet sheet){
        List<RequestPFEDTO> sujetsPfe = new ArrayList<>();
        DataFormatter formater = new DataFormatter();
        List<String> errors = new ArrayList();
        // The last row is uncluded
        for(int i =1; i <= sheet.getLastRowNum();i++){
            Row row = sheet.getRow(i);
            if(row == null) continue;
            if(isRowEmpty(row, formater)) continue;
            String sujet = formater.formatCellValue(row.getCell(0)).trim();
            String rawCnes = formater.formatCellValue(row.getCell(1)).trim();
            String rawFiliere = formater.formatCellValue(row.getCell(2)).trim();
            Set<String> cnes = Arrays.stream(rawCnes.split(","))
                    .map(String::trim)
                    .map(s -> s.replace("\u00A0", ""))
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());

            String rawLangue = formater.formatCellValue(row.getCell(3)).trim();
            
            if (rawLangue == null || rawLangue.trim().isEmpty()) {
                throw new PFEImportValidationException(List.of("Ligne " + (i + 1) + " -> Langue invalide: " + rawLangue));
            }
            Filiere filiere = Filiere.valueOf(rawFiliere);
            RequestPFEDTO pfedto = new RequestPFEDTO(cnes, sujet,filiere,rawLangue.toUpperCase());

            Set<ConstraintViolation<RequestPFEDTO>> violations = validator.validate(pfedto);
            if (!violations.isEmpty()) {
                String rowErrors = violations.stream()
                        .map(v -> v.getPropertyPath() + " : " + v.getMessage())
                        .collect(Collectors.joining(", "));
                errors.add("Ligne " + (i + 1) + " -> " + rowErrors);
            }
            sujetsPfe.add(pfedto);
        }
        if(!errors.isEmpty()){
            throw new PFEImportValidationException(errors);
        }
        return sujetsPfe;
    }

    private Set<String> ValidateEtudiants(List<RequestPFEDTO> sujetPFEs,List<String> etudiants){
        Set<String> excelCnes = sujetPFEs.stream()
                .flatMap(dto -> dto.cnes().stream())
                // toList() : returns immutable list
                // Use collect() :
                .collect(Collectors.toSet());
                excelCnes.removeAll(etudiants);
                return excelCnes;
            }
            
            
            
            
@Override
@Transactional
public void appliquerAffectation(Long versionId) {

    ImportVersion version = versionrepo.findById(versionId)
            .orElseThrow(() -> new BusinessException("Version introuvable"));

    ((PFERepository) repository).clearEncadrantByVersion(version);
    encadrantrepo.deleteByVersion(version);

    List<PFE> pfes = new ArrayList<>(((PFERepository) repository).findByVersion(version));
    List<Prof> profs = profrepo.findByVersion(version);

    if (profs.isEmpty()) {
        throw new BusinessException("Aucun professeur disponible.");
    }

    List<Encadrant> encadrants = profs.stream()
            .map(prof -> Encadrant.builder()
                    .prof(prof)
                    .pfes(new ArrayList<>())
                    .version(version)
                    .build())
            .collect(Collectors.toList());

    Collections.shuffle(encadrants);
    Collections.shuffle(pfes);

    int baseCapacity = pfes.size() / encadrants.size();

    Map<Encadrant, Integer> capacity = new HashMap<>();
    for (Encadrant e : encadrants) {
        capacity.put(e, baseCapacity);
    }

    List<PFE> unassigned = new ArrayList<>();
    for (PFE pfe : pfes) {

        // Seperating language profs from technical ones
        // Selection only profs that who hit the limit 
        // partitionBy is like partition() in scala 
        Map<Boolean,List<Encadrant>> candidates = encadrants
        .stream()
        .filter(e -> capacity.get(e) > 0)
        .collect(Collectors.partitioningBy(e -> matchesLanguage(e, pfe)));

        List<Encadrant> langues = candidates.get(Boolean.TRUE);
        List<Encadrant> techs = candidates.get(Boolean.FALSE);

        if(!langues.isEmpty()){
            Encadrant chosen = langues.stream()
            .min(Comparator.comparingInt(e -> e.getPfes().size()))
            .orElseThrow();
            assign(pfe, chosen);
            capacity.put(chosen, capacity.get(chosen) - 1);
        }else if(!techs.isEmpty()){
            Encadrant chosen = techs.stream()
            .min(Comparator.comparingInt(e -> e.getPfes().size()))
            .orElseThrow();
            assign(pfe, chosen);
            capacity.put(chosen, capacity.get(chosen) - 1);
        }else {
            unassigned.add(pfe);
        }
    }
    if (!unassigned.isEmpty()){
        Random random = new Random();
        Iterator<PFE> its = unassigned.iterator();
        while(its.hasNext()){
            PFE pfe = its.next();
            List <Encadrant> availableEnc = encadrants.stream()
            .filter(e-> e.getPfes().size() == baseCapacity)
            .toList();
            Encadrant e = availableEnc.get(random.nextInt(availableEnc.size()));
            assign(pfe,e);
            its.remove();
        }
    }

    encadrantrepo.saveAll(encadrants);
    repository.saveAll(pfes);
}

private void assign(PFE pfe, Encadrant e) {
    pfe.setEncadrant(e);
    pfe.setStatus(Status.CONFIRME);
    e.getPfes().add(pfe);
}

private boolean matchesLanguage(Encadrant e, PFE pfe) {

    if (pfe.getLangue() == null) return false;
    return pfe.getLangue().equals(e.getProf().getSpecialite());
}




    @Override
    public void createPVFolder(Long id ){
        ImportVersion version = versionrepo.findById(id).get();
        List<Encadrant> encadrants = encadrantrepo.findByVersion(version);
        encadrants.forEach(fsService::createPVFolder);
    }


    @Override
    public byte[] exportPFEExcel(Long id) throws IOException {
        ImportVersion current_version = versionrepo.findById(id).get();
        List<Encadrant> encdrant = encadrantrepo.findByVersion(current_version);
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
                                ,(map1, map2) -> { map1.putAll(map2); return map1; }
                        ));
        return ExcelGenerator.exportPFEAffectationSheet(affectations);
    }
    @Override
    public byte[] exportPFEPDF(Long id) throws IOException {
        ImportVersion current_version = versionrepo.findById(id).get();
        List<Encadrant> encdrant = encadrantrepo.findByVersion(current_version);
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
                                ,(map1, map2) -> { map1.putAll(map2); return map1; }
                        ));
        return PDFGenerator.exportAffectationPDF(affectations);
    }


}
