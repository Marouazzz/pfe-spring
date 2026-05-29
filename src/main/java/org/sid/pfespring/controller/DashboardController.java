package org.sid.pfespring.controller;

import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.model.Jury;
import org.sid.pfespring.model.PFE;
import org.sid.pfespring.model.Prof;
import org.sid.pfespring.model.Soutenance;
import org.sid.pfespring.model.scheduling.PlannedSoutenance;
import org.sid.pfespring.model.scheduling.SchedulingSolution;
import org.sid.pfespring.repository.ImportVersionRepository;
import org.sid.pfespring.repository.JuryRepository;
import org.sid.pfespring.repository.PFERepository;
import org.sid.pfespring.repository.ProfRepository;
import org.sid.pfespring.repository.SalleRepository;
import org.sid.pfespring.repository.SoutenanceRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    private final ImportVersionRepository versionRepo;
    private final PFERepository           pfeRepo;
    private final ProfRepository          profRepo;
    private final SalleRepository         salleRepo;
    private final JuryRepository          juryRepo;
    private final SoutenanceRepository    soutenanceRepo;

    public DashboardController(
            ImportVersionRepository versionRepo,
            PFERepository           pfeRepo,
            ProfRepository          profRepo,
            SalleRepository         salleRepo,
            JuryRepository          juryRepo,
            SoutenanceRepository    soutenanceRepo) {
        this.versionRepo    = versionRepo;
        this.pfeRepo        = pfeRepo;
        this.profRepo       = profRepo;
        this.salleRepo      = salleRepo;
        this.juryRepo       = juryRepo;
        this.soutenanceRepo = soutenanceRepo;
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {

        Long versionId = (Long) session.getAttribute("versionId");

        // Aucune version → page vide
        if (versionId == null) {
            model.addAttribute("noData", true);
            return "dashboard";
        }

        ImportVersion version = versionRepo.findById(versionId).orElse(null);
        if (version == null) {
            model.addAttribute("noData", true);
            return "dashboard";
        }

        // ── Données de base ────────────────────────────────────────────
        List<PFE>        pfes    = pfeRepo.findByVersion(version);
        List<Prof>       profs   = profRepo.findByVersion(version);
        List<Jury>       jurys   = juryRepo.findAllWithRelations(version);
        int              salles  = salleRepo.findByVersionAndDisponibleTrue(version).size();

        // ── Soutenances : session d'abord, sinon BDD ───────────────────
        List<SoutenanceView> soutenances = loadSoutenances(session, version);

        // ── Statistiques globales ──────────────────────────────────────
        Set<LocalDate> joursUniques = soutenances.stream()
                .map(SoutenanceView::date)
                .collect(Collectors.toSet());

        Map<String, Object> statsGlobales = new LinkedHashMap<>();
        statsGlobales.put("totalPfes",        pfes.size());
        statsGlobales.put("totalProfs",        profs.size());
        statsGlobales.put("totalSoutenances",  soutenances.size());
        statsGlobales.put("totalSalles",       salles);
        statsGlobales.put("totalJours",        joursUniques.size());

        // ── Chart 1 : PFEs encadrés par prof ──────────────────────────
        Map<String, Long> pfesParEncMap = pfes.stream()
                .filter(p -> p.getEncadrant() != null && p.getEncadrant().getProf() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getEncadrant().getProf().getNom()
                                + " " + p.getEncadrant().getProf().getPrenom(),
                        Collectors.counting()));

        List<Map<String, Object>> pfesParEncadrant = pfesParEncMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("nom",   e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());

        // Seuils équité
        double moyenne  = pfesParEncadrant.stream()
                .mapToLong(m -> (Long) m.get("count"))
                .average().orElse(0);
        int seuilMin = (int) Math.floor(moyenne) - 1;
        int seuilMax = (int) Math.ceil(moyenne)  + 1;
        statsGlobales.put("seuilMin", Math.max(0, seuilMin));
        statsGlobales.put("seuilMax", seuilMax);

        // ── Chart 2 : Soutenances par filière ─────────────────────────
        Map<String, Long> filMap = soutenances.stream()
                .collect(Collectors.groupingBy(SoutenanceView::filiere, Collectors.counting()));

        List<Map<String, Object>> soutenancesParFiliere = filMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("filiere", e.getKey());
                    m.put("count",   e.getValue());
                    return m;
                })
                .sorted(Comparator.comparing(m -> (String) m.get("filiere")))
                .collect(Collectors.toList());

        // ── Chart 3 : Participations aux jurys par prof ───────────────
        Map<String, Integer> participations = new LinkedHashMap<>();
        for (Jury j : jurys) {
            if (j.getEncadrant() != null) inc(participations, nomProf(j.getEncadrant()));
            if (j.getProf1()     != null) inc(participations, nomProf(j.getProf1()));
            if (j.getProf2()     != null) inc(participations, nomProf(j.getProf2()));
        }
        List<Map<String, Object>> soutenancesParProf = participations.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("nom",   e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());

        // ── Anomalies équité d'encadrement ────────────────────────────
        List<Map<String, Object>> anomaliesEncadrement = new ArrayList<>();
        for (Map<String, Object> entry : pfesParEncadrant) {
            long count = (Long) entry.get("count");
            if (count < seuilMin) {
                anomaliesEncadrement.add(Map.of(
                        "prof",    entry.get("nom"),
                        "message", "Sous-chargé (" + count + " PFE(s) < seuil " + seuilMin + ")",
                        "type",    "sous"));
            } else if (count > seuilMax) {
                anomaliesEncadrement.add(Map.of(
                        "prof",    entry.get("nom"),
                        "message", "Surchargé (" + count + " PFE(s) > seuil " + seuilMax + ")",
                        "type",    "sur"));
            }
        }

        // ── Anomalies planning ────────────────────────────────────────
        List<String> anomaliesPlanning = detecterAnomaliesPlanning(soutenances);

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

    // ─── Chargement soutenances : session ou BDD ──────────────────────────

    private List<SoutenanceView> loadSoutenances(HttpSession session, ImportVersion version) {

        // Priorité : solution en session (calcul récent non encore persisté ou déjà validé)
        SchedulingSolution sol =
                (SchedulingSolution) session.getAttribute("validatedSolution");
        if (sol != null && !sol.getSoutenancesPlanifiees().isEmpty()) {
            return sol.getSoutenancesPlanifiees().stream()
                    .map(ps -> new SoutenanceView(
                            ps.getJury(),
                            ps.getSlot() != null ? ps.getSlot().getDate()      : null,
                            ps.getSlot() != null ? ps.getSlot().getHeureDebut() : null,
                            ps.getSlot() != null ? ps.getSlot().getHeureFin()   : null,
                            filiereFromPs(ps)))
                    .collect(Collectors.toList());
        }

        // Fallback : table soutenances BDD
        return soutenanceRepo
                .findByVersionOrderByDateSoutenanceAscHeureDebutAscSalleNomSalleAsc(version)
                .stream()
                .map(s -> new SoutenanceView(
                        s.getJury(),
                        s.getDateSoutenance(),
                        s.getHeureDebut(),
                        s.getHeureFin(),
                        filiereFromSoutenance(s)))
                .collect(Collectors.toList());
    }

    // ─── Détection anomalies planning ─────────────────────────────────────

    private List<String> detecterAnomaliesPlanning(List<SoutenanceView> soutenances) {
        List<String> anomalies = new ArrayList<>();

        // Grouper par prof (encadrant + prof1 + prof2) → liste ordonnée par date+heure
        Map<String, List<SoutenanceView>> parProf = new HashMap<>();
        for (SoutenanceView sv : soutenances) {
            if (sv.jury() == null || sv.date() == null) continue;
            addForProf(parProf, sv, sv.jury().getEncadrant());
            addForProf(parProf, sv, sv.jury().getProf1());
            addForProf(parProf, sv, sv.jury().getProf2());
        }

        for (Map.Entry<String, List<SoutenanceView>> e : parProf.entrySet()) {
            String           profNom = e.getKey();
            List<SoutenanceView> sl  = e.getValue().stream()
                    .filter(sv -> sv.date() != null && sv.heureDebut() != null)
                    .sorted(Comparator.comparing(SoutenanceView::date)
                            .thenComparing(SoutenanceView::heureDebut))
                    .toList();

            for (int i = 0; i < sl.size() - 1; i++) {
                SoutenanceView a = sl.get(i);
                SoutenanceView b = sl.get(i + 1);

                if (!a.date().equals(b.date())) continue;

                // Chevauchement
                if (a.heureFin() != null && b.heureDebut() != null
                        && a.heureFin().isAfter(b.heureDebut())) {
                    anomalies.add(profNom + " — chevauchement le " + a.date()
                            + " entre " + a.heureDebut() + " et " + b.heureDebut());
                    continue;
                }

                // Repos < 1h
                if (a.heureFin() != null && b.heureDebut() != null) {
                    long pauseMin = Duration.between(a.heureFin(), b.heureDebut()).toMinutes();
                    if (pauseMin >= 0 && pauseMin < 60) {
                        anomalies.add(profNom + " — repos insuffisant le " + a.date()
                                + " (" + pauseMin + " min entre "
                                + a.heureFin() + " et " + b.heureDebut() + ")");
                    }
                }
            }
        }

        return anomalies;
    }

    // ─── Utilitaires ──────────────────────────────────────────────────────

    private void addForProf(Map<String, List<SoutenanceView>> map, SoutenanceView sv, Prof p) {
        if (p == null) return;
        map.computeIfAbsent(nomProf(p), k -> new ArrayList<>()).add(sv);
    }

    private static String nomProf(Prof p) {
        return p.getNom() + " " + p.getPrenom();
    }

    private static void inc(Map<String, Integer> map, String key) {
        map.merge(key, 1, Integer::sum);
    }

    private static String filiereFromPs(PlannedSoutenance ps) {
        if (ps.getPfe() == null || ps.getPfe().getEtudiants() == null
                || ps.getPfe().getEtudiants().isEmpty()) return "N/A";
        return ps.getPfe().getEtudiants().iterator().next().getFiliere().name();
    }

    private static String filiereFromSoutenance(Soutenance s) {
        if (s.getPfe() == null || s.getPfe().getEtudiants() == null
                || s.getPfe().getEtudiants().isEmpty()) return "N/A";
        return s.getPfe().getEtudiants().iterator().next().getFiliere().name();
    }

    // ─── Record interne ───────────────────────────────────────────────────

    private record SoutenanceView(
            Jury             jury,
            LocalDate        date,
            java.time.LocalTime heureDebut,
            java.time.LocalTime heureFin,
            String           filiere) {}
}