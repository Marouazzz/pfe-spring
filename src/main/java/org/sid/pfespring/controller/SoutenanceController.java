package org.sid.pfespring.controller;

import org.sid.pfespring.services.SalleService;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;

/**
 * Déclenche la planification des soutenances.
 * Aucun 2ème fichier n'est demandé : la date de début a été
 * stockée en session lors de l'upload initial.
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
    public Object affecterSoutenances(HttpServletRequest req) throws Exception {

        HttpSession session = req.getSession(false);
//used flags
        //  Vérification des étapes
        if (session == null ||
                session.getAttribute("etape1") == null ||
                session.getAttribute("etape2") == null ||
                session.getAttribute("etape3") == null) {

            return "redirect:/erreur";
        }

        LocalDate dateDebut = (LocalDate) session.getAttribute("dateDebut");

        salleService.affecterSalles(dateDebut);
        byte[] excel = salleService.exportPlanningExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=planning_soutenances.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }
}