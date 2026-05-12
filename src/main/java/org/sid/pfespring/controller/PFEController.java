package org.sid.pfespring.controller;



import java.io.IOException;
import java.time.Year;

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
public class PFEController{

    PFEService pFEService;

    public PFEController(PFEService service) {
        this.pFEService = service;
    }

    @GetMapping("/affectations")
    public ResponseEntity<byte[]> affecterProfEtudiants(@RequestParam("id") Long id,HttpSession session) throws IOException {
        int annee = Year.now().getValue();
        pFEService.appliquerAffectation(id);
        session.setAttribute("etape2", true);
        session.setAttribute("versionId", id);
        session.removeAttribute("etape3");
        session.removeAttribute("etape4");
        byte [] files = pFEService.exportPFEAffectation(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=pfe_affectations_" + annee + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(files);
    }

}
