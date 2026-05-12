package org.sid.pfespring.controller;


import java.io.IOException;
import java.time.Year;

import org.sid.pfespring.services.JuryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/jurys")
public class JuryController {

    private final JuryService juryService;

    public JuryController(JuryService juryService) {
        this.juryService = juryService;
    }

//    @GetMapping("/affectations")
//    public ResponseEntity<byte[]> affecterJury(@RequestParam("id") Long id,HttpSession session) throws IOException {
//        juryService.affecterJury(id);
//        session.setAttribute("etape3",true);
//        byte[] fichier = juryService.exportJuryExcel(id);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=jury_affectations.xlsx")
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(fichier);
//    }
    @GetMapping("/affectations")
    public String affecterJury(@RequestParam("id") Long id, HttpSession session) throws IOException {
        if (session.getAttribute("etape1") == null ||
        session.getAttribute("etape2") == null) {
            throw new RuntimeException("Vous devez compléter les étapes précédentes avant d'affecter les jurys.");
        }
        
        juryService.affecterJury(id);
        session.setAttribute("etape3", true);
        session.setAttribute("versionId", id);
        return "redirect:/home";
    }
    
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadExcel(@RequestParam("id") Long id,HttpSession session) throws IOException{
                if (session.getAttribute("etape3") == null) {
    throw new RuntimeException("Affectation des Encadrants non générée");
}
        int annee = Year.now().getValue();
        byte[] fichier = juryService.exportJuryExcel(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=jury_affectations_" + annee + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fichier);
    }
}