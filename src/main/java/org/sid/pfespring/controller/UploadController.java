package org.sid.pfespring.controller;


import org.sid.pfespring.services.EtudiantService;
import org.sid.pfespring.services.PFEService;
import org.sid.pfespring.services.ProfService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.sid.pfespring.dto.RequestPFEDTO;
import java.util.List;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/home")
public class UploadController{

    private EtudiantService etudiantService;
    private ProfService profService;
    private PFEService pfeService;

    public UploadController(EtudiantService etudiantService, PFEService pfeService, ProfService profService) {
        this.etudiantService = etudiantService;
        this.pfeService = pfeService;
        this.profService = profService;
    }


    


    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,HttpSession session){
        etudiantService.importFromExcel(file);
        profService.importFromExcel(file);
        List<RequestPFEDTO> pfes = pfeService.readExcel(file);
        session.setAttribute("pfes", pfes);
        return "upload";
    }

    @GetMapping
    public String welcomePage(){
        return "upload";
    }
}
