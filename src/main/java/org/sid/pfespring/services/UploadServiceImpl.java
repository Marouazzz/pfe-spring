package org.sid.pfespring.services;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.sid.pfespring.dto.ResponseUploadDTO;
import org.sid.pfespring.exception.InvalidSheetStructureException;
import org.sid.pfespring.model.ImportVersion;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import jakarta.transaction.Transactional;

@Service
public class UploadServiceImpl implements UploadService {

    private EtudiantService etudiantService;
    private ProfService profService;
    private PFEService pfeService;
    private SalleService salleService;
    private ImportVersionService versionService;


    public UploadServiceImpl(EtudiantService etudiantService, ProfService profService, PFEService pfeService,SalleService salleService,ImportVersionService versionService) {
        this.etudiantService = etudiantService;
        this.profService = profService;
        this.pfeService = pfeService;
        this.salleService = salleService;
        this.versionService = versionService;
    }

    @Transactional
    @Override
    public ResponseUploadDTO importSheets(MultipartFile file) {
        try(Workbook wb = WorkbookFactory.create(file.getInputStream())){
        validateSheetNames(wb);
        ImportVersion version = versionService.addVersion();
        Sheet etduiantSheet  = wb.getSheet("etu");
        Sheet profSheet = wb.getSheet("profs");
        Sheet pfeSheet = wb.getSheet("pfes");
        Sheet salleSheet =  wb.getSheet("salles");
        Sheet soutenanceSheet = wb.getSheet("jours_soutenances");
        etudiantService.importFromExcel(etduiantSheet,version);
        profService.importFromExcel(profSheet,version);
        pfeService.importFromExcel(pfeSheet,version);
        salleService.importFromExcel(salleSheet,version);
        LocalDate dateDebut = salleService.importDateDebut(soutenanceSheet);
        Long versionId = version.getId();
        ResponseUploadDTO dto  = new ResponseUploadDTO(versionId, dateDebut);
        return dto;
    }catch(IOException ioe){
        throw new RuntimeException("Erreur lors de la lecture  du fichier");
    }
    }

    private void validateSheetNames(Workbook wb){
        List<String> required  = List.of(
            "etu",
            "profs",
            "pfes",
            "salles",
            "jours_soutenances"
        );

        List<String> sheetNames = new ArrayList<>();

        for(int i=0;i < wb.getNumberOfSheets();i++){
            sheetNames.add(wb.getSheetName(i));
        }

        List<String> missingSheets = required.stream()
                                     .filter(sheet -> !sheetNames.contains(sheet))
                                     .toList();

        if (!missingSheets.isEmpty()){
            throw new InvalidSheetStructureException("Impossible de trouver les Sheets "+ missingSheets.toString() +
            "\n Assurez-vous que votre fichier Excel possede les Sheets suivant :" + required.toString());
        }
    }
    
    

    

    
}