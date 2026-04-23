package org.sid.pfespring.controller;

import lombok.RequiredArgsConstructor;
import org.sid.pfespring.dto.ResponseSalleDTO;
import org.sid.pfespring.dto.ResponseSoutenanceDTO;
import org.sid.pfespring.services.SalleService;
import org.sid.pfespring.services.SoutenanceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/soutenances")
@RequiredArgsConstructor
public class SoutenanceController {

    private final SalleService      salleService;
    private final SoutenanceService soutenanceService;

    // 1 Importer salles + jours, puis lancer l'affectation

    @PostMapping("/affectations")
    public ResponseEntity<List<ResponseSoutenanceDTO>> affecterSalles(
            @RequestParam("file") MultipartFile file) throws Exception {

        // Import des salles (sheet "salles")
        salleService.importFromExcel(file.getInputStream());

        // Import des jours (sheet "jours_soutenances")
        List<LocalDate> jours = salleService.importJoursSoutenances(file.getInputStream());

        // Lancement de l'algorithme d'affectation
        return ResponseEntity.ok(salleService.affecterSalles(jours));
    }

    // 2 Exporter le planning en Excel
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportPlanning() throws Exception {
        byte[] excel = salleService.exportPlanningExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=planning_soutenances.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }


}