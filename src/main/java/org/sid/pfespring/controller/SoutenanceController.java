package org.sid.pfespring.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;

import org.sid.pfespring.services.SalleService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import jakarta.servlet.http.HttpSession;
/**
 * Déclenche la planification des soutenances.
 * Aucun 2ème fichier n'est demandé : la date de début a été
 * stockée en model lors de l'upload initial.
 *
 * GET /soutenances/affectations → génère et télécharge le planning Excel.
 */
@Controller
@RequestMapping("/soutenances")
public class SoutenanceController {

    private final SalleService salleService;

    public SoutenanceController(SalleService salleService) {
        this.salleService = salleService;
    }

    @GetMapping("/planifier")
    public String affecterSoutenances(@RequestParam("id") Long id,HttpSession session) throws Exception {
//used flags
        session.setAttribute("etape4", true);
        //  Vérification des étapes
        if (session.getAttribute("etape1") == null ||
            session.getAttribute("etape2") == null ||
            session.getAttribute("etape3") == null) {
                return "redirect:/erreur";
            }

        LocalDate dateDebut = (LocalDate) session.getAttribute("dateDebut");
        salleService.affecterSalles(dateDebut,id);
        return "redirect:/home";
    }
    
    @GetMapping("/download")
    public ResponseEntity<byte[]> exportFile(@RequestParam("id") Long id,HttpSession session) throws IOException{
                if (session.getAttribute("etape4") == null) {
    throw new RuntimeException("Affectation non générée");
}
        int annee = Year.now().getValue();
        byte[] excel = salleService.exportPlanningExcel(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=planning_soutenances_" + annee + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }
}