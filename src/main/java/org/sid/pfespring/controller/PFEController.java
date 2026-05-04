package org.sid.pfespring.controller;



import java.io.IOException;

import org.sid.pfespring.dto.RequestPFEDTO;
import org.sid.pfespring.dto.ResponsePFEDTO;
import org.sid.pfespring.services.PFEService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public ResponseEntity<byte[]> affecterProfEtudiants(@RequestParam("id") Long id,HttpSession session) throws IOException {
        pFEService.appliquerAffectation(id);
        session.setAttribute("etape2", true);
        byte [] files = pFEService.exportPFEAffectation(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=pfe_affectations.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(files);
    }

}
