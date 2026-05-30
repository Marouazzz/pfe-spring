package org.sid.pfespring.controller;
import org.sid.pfespring.model.scheduling.SchedulingSolution;
import org.sid.pfespring.services.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

import java.util.*;

/**
 * Tableau de bord — statistiques et vérification de conformité.
 *
 * Sources de données (par priorité) :
 *   1. SchedulingSolution en session (validatedSolution) → données "live" du dernier calcul
 *   2. Table soutenances BDD (version courante)          → données persistées
 *
 * Graphiques :
 *   - PFEs encadrés par professeur (barres horizontales)
 *   - Soutenances par filière      (donut)
 *   - Participations aux jurys par prof (courbe)
 *
 * Panneau conformité :
 *   - Équité d'encadrement (seuil min/max ±1 par rapport à la moyenne)
 *   - Conformité planning  (chevauchements, repos < 1h entre 2 jurys d'un même prof)
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service){
        this.service = service;
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {

        Long versionId = (Long) session.getAttribute("versionId");

        // Aucune version → page vide
        if (versionId == null) {
            model.addAttribute("noData", true);
            return "dashboard";
        }
        // ── Soutenances : session d'abord, sinon BDD ───────────────────
        SchedulingSolution sol = (SchedulingSolution) session.getAttribute("validatedSolution");
        // ── Statistiques globales ──────────────────────────────────────
        Map<String, Object> statsGlobales = service.statsGlobales(sol, versionId);

        // ── Chart 1 : PFEs encadrés par prof ──────────────────────────
        List<Map<String, Object>> pfesParEncadrant = service.pfesParEncadrant(versionId);
        // ── Chart 2 : Soutenances par filière ─────────────────────────
        List<Map<String, Object>> soutenancesParFiliere = service.soutenancesParFiliere(sol, versionId);
        // ── Chart 3 : Participations aux jurys par prof ───────────────
        List<Map<String, Object>> soutenancesParProf = service.soutenancesParProf(versionId);

        // ── Anomalies équité d'encadrement ────────────────────────────
        List<Map<String, Object>> anomaliesEncadrement = service.anomaliesEncadrement(versionId);
        // ── Anomalies planning ────────────────────────────────────────
        List<String> anomaliesPlanning = service.detecterAnomaliesPlanning(sol, versionId);
        // ── Modèle ────────────────────────────────────────────────────
        model.addAttribute("noData",               false);
        model.addAttribute("statsGlobales",        statsGlobales);
        model.addAttribute("pfesParEncadrant",     pfesParEncadrant);
        model.addAttribute("soutenancesParFiliere", soutenancesParFiliere);
        model.addAttribute("soutenancesParProf",   soutenancesParProf);
        model.addAttribute("anomaliesEncadrement", anomaliesEncadrement);
        model.addAttribute("anomaliesPlanning",    anomaliesPlanning);
        return "dashboard";
    }
}