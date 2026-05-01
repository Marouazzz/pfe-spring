package org.sid.pfespring.services;


import org.sid.pfespring.model.ImportVersion;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadServiceImpl implements UploadService {

    private EtudiantService etudiantService;
    private ProfService profService;
    private PFEService pfeService;
    private ImportVersionService versionService;

    public UploadServiceImpl(EtudiantService etudiantService, ProfService profService, PFEService pfeService,ImportVersionService versionService) {
        this.etudiantService = etudiantService;
        this.profService = profService;
        this.pfeService = pfeService;
        this.versionService = versionService;
    }

    @Override
    public Long importSheets(MultipartFile file){
        ImportVersion version = versionService.addVersion();
        etudiantService.importFromExcel(file,version);
        profService.importFromExcel(file,version);
        pfeService.importFromExcel(file,version);
        return version.getId();
    }
    
    

    

    
}