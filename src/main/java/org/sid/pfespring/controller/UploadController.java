package org.sid.pfespring.controller;


import org.sid.pfespring.services.UploadService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/home")
public class UploadController{

    private UploadService service;

    public UploadController(UploadService service) {
        this.service = service;
    }


    


    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,Model model){
        Long id = this.service.importSheets(file);      
        model.addAttribute("versionId",id);  
        return "upload";
    }

    @GetMapping
    public String welcomePage(){
        return "upload";
    }
}
