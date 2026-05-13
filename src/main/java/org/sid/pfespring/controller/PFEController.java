package org.sid.pfespring.controller;



import java.io.IOException;
import java.time.Year;

import org.sid.pfespring.services.PFEService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

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
        throw new RuntimeException("Aucune version active. Importez d'abord le fichier Excel.");
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
    @GetMapping("/export/{format}")
    public ResponseEntity<byte[]> exportEncadrants( @PathVariable String format,HttpSession session) throws IOException {

        Long id = (Long) session.getAttribute("versionId");
        if (id == null || session.getAttribute("exportEncadrantsOk") == null) {
            throw new RuntimeException("Assurez-vous que vous avez bien deposer le fichier des PFE et que vous avez lancer l'affectation des PFE's");
        }

        byte[] files;
        String extension  = switch(format){
            case "excel" -> {
            files = pFEService.exportPFEExcel(id);
            yield ".xlsx";
        }
        case "pdf" -> {
                files = pFEService.exportPFEPDF(id);
                yield ".pdf";
            }
        default -> throw new IllegalArgumentException("Cette format est non supporte pas notre systeme.Essayez d'exporter les PFE sous les format suivanrs :\n PDF\nExcel");
        };
        int annee = Year.now().getValue();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=pfe_affectations_" + annee + extension)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(files);
    }
}

