package org.sid.pfespring.controller;


import java.io.IOException;
import java.time.Year;

import org.sid.pfespring.services.JuryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/jurys")
public class JuryController {

    private final JuryService juryService;

    public JuryController(JuryService juryService) {
        this.juryService = juryService;
    }

 /*   @GetMapping("/affectations")
    public Object affecterJury(@RequestParam("id") Long id, HttpSession session) throws IOException {

        if (session.getAttribute("etape1") == null ||
                session.getAttribute("etape2") == null) {
            return "redirect:/erreur?message=Vous devez compléter les étapes précédentes";
        }
        int annee = Year.now().getValue();
        juryService.affecterJury(id);
        session.setAttribute("etape3", true);
        session.setAttribute("versionId", id);
        session.removeAttribute("etape4");
        byte[] fichier = juryService.exportJuryExcel(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=jury_affectations_" + annee + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fichier);
    }

  */

    @PostMapping("/affecter")
    public String affecterJury(HttpSession session) {
        if (session.getAttribute("etape1") == null ||
                session.getAttribute("etape2") == null) {
            throw new RuntimeException("Vous devez compléter les étapes précédentes");
        }
        Long id = (Long) session.getAttribute("versionId");
        juryService.affecterJury(id);

        session.setAttribute("etape3", true);
        session.setAttribute("exportJurysOk", true); // ← nouveau flag pour afficher le bouton Excel
        // invalidation des autres étapes
        session.removeAttribute("etape4");
        session.removeAttribute("etape5");
        session.removeAttribute("exportSoutenancesOk");

        return "redirect:/home";
    }


    @GetMapping("/export/{format}")
    public ResponseEntity<byte[]> exportJurys(@PathVariable String format,HttpSession session) throws IOException {
        Long id = (Long) session.getAttribute("versionId");
        if (id == null || session.getAttribute("exportJurysOk") == null) {
            throw new RuntimeException("Assurez-vous que vous avez bien effectuer l'affectation des Jurys");
        }
        int annee = Year.now().getValue();
        byte[] fichier; 
        String extension  = switch(format){
            case "excel" -> {
            fichier = juryService.exportJuryExcel(id);
            yield ".xlsx";
        }
        case "pdf" -> {
            fichier = juryService.exportJuryPDF(id);
            yield ".pdf";
            }
        default -> throw new IllegalArgumentException("Cette format est non supporte pas notre systeme.Essayez d'exporter les PFE sous les format suivanrs :\n PDF\nExcel");
        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=jury_affectations_" + annee + extension)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fichier);
    }
}

