package org.sid.pfespring.controller;

import java.time.LocalDate;

import org.sid.pfespring.services.SalleService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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


    @PostMapping("/planifier")
    public String planifierSoutenances(HttpSession session) {
        if (session.getAttribute("etape1") == null ||
                session.getAttribute("etape2") == null ||
                session.getAttribute("etape3") == null) {
            throw new RuntimeException("Vous devez compléter les étapes précédentes");
        }
        Long id = (Long) session.getAttribute("versionId");
        LocalDate dateDebut = (LocalDate) session.getAttribute("dateDebut");
        salleService.affecterSalles(dateDebut, id);

        session.setAttribute("etape4", true);
        session.setAttribute("exportSoutenancesOk", true); // ← nouveau flag pour afficher le bouton Excel
        session.removeAttribute("etape5");

        return "redirect:/home";
    }


    @GetMapping("/export/{format}")
    public ResponseEntity<byte[]> exportPlanning(@PathVariable String format,HttpSession session) throws Exception {
        Long id = (Long) session.getAttribute("versionId");
        if (id == null || session.getAttribute("exportSoutenancesOk") == null) {
            return ResponseEntity.badRequest().build();
        }
        int annee = Year.now().getValue();
        byte[] excel;
        String extension  = switch(format){
            case "excel" -> {
            excel = salleService.exportPlanningExcel(id);
            yield ".xlsx";
        }
        case "pdf" -> {
                excel = salleService.exportPlanningPDF(id);
                yield ".pdf";
            }
        default -> throw new IllegalArgumentException("Cette format est non supporte pas notre systeme.Essayez d'exporter les PFE sous les format suivanrs :\n PDF\nExcel");
        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=planning_soutenances_" + annee + extension)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }

}


