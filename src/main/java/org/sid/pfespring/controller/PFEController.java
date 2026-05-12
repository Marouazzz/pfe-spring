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
import org.springframework.web.bind.annotation.PostMapping;
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

/*    @GetMapping("/affectations")
    public ResponseEntity<byte[]> affecterProfEtudiants(@RequestParam("id") Long id,HttpSession session) throws IOException {
        int annee = Year.now().getValue();
        pFEService.appliquerAffectation(id);
        session.setAttribute("etape2", true);
        session.setAttribute("versionId", id);
        session.removeAttribute("etape3");
        session.removeAttribute("etape4");
        session.removeAttribute("etape5");
        byte [] files = pFEService.exportPFEAffectation(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=pfe_affectations_" + annee + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(files);
    }

 */
@PostMapping("/affecter")
public String affecterEncadrants(HttpSession session) {
    Long id = (Long) session.getAttribute("versionId");
    if (id == null) {
        return "redirect:/erreur?message=Aucune version active. Importez d'abord le fichier Excel.";
    }
    pFEService.appliquerAffectation(id);

    // flags
    session.setAttribute("etape2", true);
    session.setAttribute("exportEncadrantsOk", true); // ← nouveau flag pour afficher le bouton Excel
   //cas reexecution
    session.removeAttribute("etape3");
    session.removeAttribute("etape4");
    session.removeAttribute("etape5");
    session.removeAttribute("exportJurysOk");
    session.removeAttribute("exportSoutenancesOk");

    return "redirect:/home";
}
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportEncadrants(HttpSession session) throws IOException {
        Long id = (Long) session.getAttribute("versionId");
        if (id == null || session.getAttribute("exportEncadrantsOk") == null) {
            return ResponseEntity.badRequest().build();
        }
        int annee = Year.now().getValue();
        byte[] files = pFEService.exportPFEAffectation(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=pfe_affectations_" + annee + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(files);
    }
}

