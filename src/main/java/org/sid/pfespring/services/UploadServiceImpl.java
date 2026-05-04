package org.sid.pfespring.services;


import java.time.LocalDate;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.sid.pfespring.dto.ResponseUploadDTO;
import org.sid.pfespring.model.ImportVersion;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    @Override
    public ResponseUploadDTO importSheets(MultipartFile file){
        try(Workbook wb = WorkbookFactory.create(file.getInputStream())){
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
        }catch(Exception  e){
            return null;
        }
    }
    
    

    

    
}