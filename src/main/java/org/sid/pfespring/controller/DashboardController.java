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
        boolean etape4 = session.getAttribute("etape4") != null;

        if (versionId == null) {
            return redirect();
        }

        ImportVersion version = versionRepository.findById(versionId).orElse(null);
        if (version == null) {
            return redirect();
        }

        if (!etape4) {
            return redirect();
        }

        List<Soutenance> soutenances = soutenanceRepository
                .findByVersionOrderByDateSoutenanceAscHeureDebutAscSalleNomSalleAsc(version);

        model.addAttribute("noData", false);
        model.addAttribute("statsGlobales", dashboardService.statsGlobales(versionId));
        model.addAttribute("pfesParEncadrant", etape2 ? dashboardService.pfesParEncadrant(versionId) : List.of());
        model.addAttribute("soutenancesParProf", dashboardService.soutenancesParProf(versionId));
        model.addAttribute("soutenancesParFiliere", dashboardService.soutenancesParFiliere(versionId));
        model.addAttribute("anomaliesEncadrement", etape2 ? dashboardService.anomaliesEncadrement(versionId) : List.of());
        model.addAttribute("anomaliesPlanning", salleService.detecterAnomalies(soutenances));

        return "dashboard";
    }

    private String redirect() {
        return "redirect:/erreur?message=Vous+devez+effectuer+la+planification+des+soutenances+avant+de+consulter+le+tableau+de+bord";
    }
}