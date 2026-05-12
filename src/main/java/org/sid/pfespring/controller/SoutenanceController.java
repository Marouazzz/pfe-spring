package org.sid.pfespring.controller;

import java.time.LocalDate;

import org.sid.pfespring.services.SalleService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.time.Year;
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

   /* @GetMapping("/planifier")
    public Object affecterSoutenances(@RequestParam("id") Long id,HttpSession session) throws Exception {
//used flags


        //  Vérification des étapes
        if (session.getAttribute("etape1") == null ||
            session.getAttribute("etape2") == null ||
            session.getAttribute("etape3") == null) {

            return "redirect:/erreur?message=Vous devez compléter les étapes précédentes";
        }

        int annee = Year.now().getValue();
        LocalDate dateDebut = (LocalDate) session.getAttribute("dateDebut");
        salleService.affecterSalles(dateDebut,id);
        session.setAttribute("etape3", true);
        session.setAttribute("versionId", id);
        session.removeAttribute("etape4");
        byte[] excel = salleService.exportPlanningExcel(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=planning_soutenances_" + annee + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }

    */

    @PostMapping("/planifier")
    public String planifierSoutenances(HttpSession session) {
        if (session.getAttribute("etape1") == null ||
                session.getAttribute("etape2") == null ||
                session.getAttribute("etape3") == null) {
            return "redirect:/erreur?message=Vous devez compléter les étapes précédentes";
        }
        Long id = (Long) session.getAttribute("versionId");
        LocalDate dateDebut = (LocalDate) session.getAttribute("dateDebut");
        salleService.affecterSalles(dateDebut, id);

        session.setAttribute("etape4", true);
        session.setAttribute("exportSoutenancesOk", true); // ← nouveau flag pour afficher le bouton Excel
        session.removeAttribute("etape5");

        return "redirect:/home";
    }


    @GetMapping("/export")
    public ResponseEntity<byte[]> exportPlanning(HttpSession session) throws Exception {
        Long id = (Long) session.getAttribute("versionId");
        if (id == null || session.getAttribute("exportSoutenancesOk") == null) {
            return ResponseEntity.badRequest().build();
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


