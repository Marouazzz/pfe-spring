package org.sid.pfespring.controller;

import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.model.Soutenance;
import org.sid.pfespring.repository.ImportVersionRepository;
import org.sid.pfespring.repository.SoutenanceRepository;
import org.sid.pfespring.services.DashboardService;
import org.sid.pfespring.services.SalleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final SalleService salleService;
    private final SoutenanceRepository soutenanceRepository;
    private final ImportVersionRepository versionRepository;


    public DashboardController(DashboardService dashboardService,
                               SalleService salleService,
                               SoutenanceRepository soutenanceRepository,
                               ImportVersionRepository versionRepository) {
        this.dashboardService     = dashboardService;
        this.salleService         = salleService;
        this.soutenanceRepository = soutenanceRepository;
        this.versionRepository    = versionRepository;
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        Long versionId = (Long) session.getAttribute("versionId");
        boolean etape2 = session.getAttribute("etape2") != null;
        boolean etape3 = session.getAttribute("etape3") != null;
        boolean etape4 = session.getAttribute("etape4") != null;



        ImportVersion version = versionRepository.findById(versionId).orElse(null);
        if (version == null) {
            model.addAttribute("noData", false);
            model.addAttribute("statsGlobales", Map.of(
                    "totalPfes", 0L, "totalProfs", 0L,
                    "totalSoutenances", 0L, "totalSalles", 0L,
                    "totalJours", 0L, "seuilMin", 0L, "seuilMax", 0L
            ));
            model.addAttribute("pfesParEncadrant", List.of());
            model.addAttribute("soutenancesParProf", List.of());
            model.addAttribute("soutenancesParFiliere", List.of());
            model.addAttribute("anomaliesEncadrement", List.of());
            model.addAttribute("anomaliesPlanning", List.of());
            return "dashboard";
        }
        if (versionId == null) {
            model.addAttribute("noData", false);
            model.addAttribute("statsGlobales", Map.of(
                    "totalPfes", 0L, "totalProfs", 0L,
                    "totalSoutenances", 0L, "totalSalles", 0L,
                    "totalJours", 0L, "seuilMin", 0L, "seuilMax", 0L
            ));
            model.addAttribute("pfesParEncadrant", List.of());
            model.addAttribute("soutenancesParProf", List.of());
            model.addAttribute("soutenancesParFiliere", List.of());
            model.addAttribute("anomaliesEncadrement", List.of());
            model.addAttribute("anomaliesPlanning", List.of());
            return "dashboard";
        }
        List<Soutenance> soutenances = soutenanceRepository
                .findByVersionOrderByDateSoutenanceAscHeureDebutAscSalleNomSalleAsc(version);
// Stats de base toujours visibles si versionId existe
        model.addAttribute("noData", false);
        model.addAttribute("statsGlobales", dashboardService.statsGlobales(versionId));
        model.addAttribute("pfesParEncadrant", etape2 ? dashboardService.pfesParEncadrant(versionId) : List.of());
        model.addAttribute("soutenancesParProf", etape4 ? dashboardService.soutenancesParProf(versionId) : List.of());
        model.addAttribute("soutenancesParFiliere", etape4 ? dashboardService.soutenancesParFiliere(versionId) : List.of());
        model.addAttribute("anomaliesEncadrement", etape2 ? dashboardService.anomaliesEncadrement(versionId) : List.of());
        model.addAttribute("anomaliesPlanning", etape4 ? salleService.detecterAnomalies(soutenances) : List.of());
        return "dashboard";
    }
}