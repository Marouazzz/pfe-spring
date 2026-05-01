package org.sid.pfespring.controller;



import org.sid.pfespring.dto.RequestPFEDTO;
import org.sid.pfespring.dto.ResponsePFEDTO;

import org.sid.pfespring.services.PFEService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/affectations")
    public ResponseEntity<byte[]> affecterProfEtudiants(@RequestParam("id") Long id) throws IOException {
        pFEService.appliquerAffectation(id);
        byte [] files = pFEService.exportPFEAffectation(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=pfe_affectations.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(files);
    }



    


    
}
