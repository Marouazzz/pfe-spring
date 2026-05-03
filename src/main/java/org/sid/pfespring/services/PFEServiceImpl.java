package org.sid.pfespring.services;
import org.sid.pfespring.exception.BusinessException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
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
import org.sid.pfespring.model.ImportVersion;
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
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

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


    @Override
    public void importFromExcel(MultipartFile file,ImportVersion version) {
        List<RequestPFEDTO> pfedtos = readExcel(file);
        List<Etudiant> etudiants = etudrepo.findByVersion(version);
        Map<String, Etudiant> etudiantMap = etudiants.stream()
        .collect(Collectors.toMap(Etudiant::getCne, e -> e));
        List<String> cnes = etudiantMap.keySet().stream().toList();
        Set<String> anomalies = ValidateEtudiants(pfedtos,cnes);

        // Try to create your Own Exception
//        if (!anomalies.isEmpty()) {
//        throw new RuntimeException("CNEs introuvables: " + anomalies);
//    }
        if (!anomalies.isEmpty())
            throw new BusinessException("Données invalides dans le fichier : CNEs introuvables : " + anomalies);

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
    private List<RequestPFEDTO> readExcel(MultipartFile file){
        try(Workbook workbook = WorkbookFactory.create(file.getInputStream())){
            // Validation Exception will be handled later
            List<RequestPFEDTO> sujetsPfe = new ArrayList<>();
            Sheet sheet = workbook.getSheet("pfe_v2");
            DataFormatter formater = new DataFormatter();
            // The last row is uncluded
            for(int i =1; i <= sheet.getLastRowNum();i++){
                Row row = sheet.getRow(i);
                if(row == null) continue;
                if(isRowEmpty(row, formater)) continue;
                String sujet = formater.formatCellValue(row.getCell(0)).trim();
                String rawCnes = formater.formatCellValue(row.getCell(1));
                Set<String> cnes = Arrays.stream(rawCnes.split(","))
                    .map(String::trim)
                    .map(s -> s.replace("\u00A0", ""))
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());
                String langue = formater.formatCellValue(row.getCell(3))
                        .trim().toUpperCase()
                        .replace("É", "E").replace("È", "E").replace("Ç", "C");
                if (langue.isBlank()) langue = null;
                RequestPFEDTO pfedto = new RequestPFEDTO(cnes, sujet, null,langue);

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
public void appliquerAffectation(Long versionId) {
    ImportVersion version = versionrepo.findById(versionId).get();
    // delete link mbin enc et pfe then delete enca
    ((PFERepository) repository).clearEncadrantByVersion(version);
    encadrantrepo.deleteByVersion(version);
    // Charger apres nettoyage → Hibernate a une vue propre
    List<PFE> pfes = ((PFERepository) repository).findByVersion(version);

    List<Specialite> langues = List.of(Specialite.ANGLAIS, Specialite.FRANCAIS);
    List<Prof> profs = profrepo.findByVersionAndSpecialiteNotIn(version,langues);
    // Create your Own Exception
//    if (profs.isEmpty()) {
//        throw new RuntimeException("Aucun prof disponible");
//    }
    if (profs.isEmpty())
        throw new BusinessException("Aucun professeur disponible.");

    // Convert prof to encadrant
    List<Encadrant> encadrants = profs.stream()
            .map(
                prof -> Encadrant.builder()
                    .prof(prof)
                    .pfes(new ArrayList<>())
                    .version(version)
                    .build()
                )
            .collect(Collectors.toList());

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
            pfe.setStatus(Status.CONFIRME);
            encadrant.getPfes().add(pfe);
            index++;
        }
        reste--;
    }
    // 8. Sauvegarde (important)
    encadrantrepo.saveAll(encadrants);
    encadrants.forEach(fsService::createPVFolder);
    repository.saveAll(pfes);
}
  @Override
  public byte[] exportPFEAffectation(Long id) throws IOException {
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


}
