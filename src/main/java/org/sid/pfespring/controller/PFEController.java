package org.sid.pfespring.controller;

import java.util.List;

import org.sid.pfespring.dto.RequestPFEDTO;
import org.sid.pfespring.dto.ResponsePFEDTO;
import org.sid.pfespring.model.Etudiant;
import org.sid.pfespring.model.Prof;
import org.sid.pfespring.services.PFEService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;


// @RestController
@Controller
@RequestMapping("/pfes")
public class PFEController extends AbstractController<RequestPFEDTO, ResponsePFEDTO>{

    PFEService pFEService;

    public PFEController(PFEService service) {
        super(service);
        this.pFEService = service;
    }

/*     @PostMapping("/import")
    public ResponseEntity<List<ResponsePFEDTO>> importFromExcel(@RequestParam("file") MultipartFile file){
        List<ResponsePFEDTO> pfes = pFEService.importFromExcel(file);
        return ResponseEntity.status(HttpStatus.CREATED)
        .body(pfes);
    } */

    @GetMapping("/affectations")
    public ResponseEntity<byte[]> affecterProfEtudiants(HttpServletRequest req) throws IOException {
        HttpSession session = req.getSession(false);
        session.setAttribute("etape2", true);
        List<RequestPFEDTO> pfes =  (List<RequestPFEDTO>) session.getAttribute("pfes");
        pFEService.appliquerAffectation(pfes);
        byte [] files = pFEService.exportPFEAffectation();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=pfe_affectations.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(files);
        
    }



    


    
}
