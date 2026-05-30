package org.sid.pfespring.services;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {
    
    private final ImportVersionRepository versionRepo;
    private final PFERepository pfeRepo;
    private final ProfRepository profRepo;
    private final SalleRepository salleRepo;
    private final JuryRepository juryRepo;
    private final SoutenanceRepository soutenanceRepo;



    public DashboardServiceImpl(ImportVersionRepository versionRepo, PFERepository pfeRepo, ProfRepository profRepo,
            SalleRepository salleRepo, JuryRepository juryRepo, SoutenanceRepository soutenanceRepo) {
        this.versionRepo = versionRepo;
        this.pfeRepo = pfeRepo;
        this.profRepo = profRepo;
        this.salleRepo = salleRepo;
        this.juryRepo = juryRepo;
        this.soutenanceRepo = soutenanceRepo;
    }

    private ImportVersion getVersion(Long versionId) {
        return versionRepo.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version introuvable : " + versionId));
    }

    @Override
    public Map<String, Object> statsGlobales(SchedulingSolution sol,Long versionId) {
        ImportVersion version = getVersion(versionId);
        List<SoutenanceView> soutenances = loadSoutenances(sol, version);
                // ── Statistiques globales ──────────────────────────────────────
        Set<LocalDate> joursUniques = soutenances.stream()
                .map(SoutenanceView::date)
                .collect(Collectors.toSet());
                // ── Données de base ────────────────────────────────────────────
        List<PFE>        pfes    = pfeRepo.findByVersion(version);
        List<Prof>       profs   = profRepo.findByVersion(version);
        List<Jury>       jurys   = juryRepo.findAllWithRelations(version);
        int              salles  = salleRepo.findByVersionAndDisponibleTrue(version).size();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalPfes",        pfes.size());
        stats.put("totalProfs",        profs.size());
        stats.put("totalSoutenances",  soutenances.size());
        stats.put("totalSalles",       salles);
        stats.put("totalJours",        joursUniques.size());

        
        // ── Chart 1 : PFEs encadrés par prof ──────────────────────────
        List<Map<String, Object>> pfesParEncadrant = pfesParEncadrant(versionId);

        // Seuils équité
        double moyenne  = pfesParEncadrant.stream()
                .mapToLong(m -> (Long) m.get("count"))
                .average().orElse(0);
        int seuilMin = (int) Math.floor(moyenne) - 1;
        int seuilMax = (int) Math.ceil(moyenne)  + 1;
        stats.put("seuilMin", Math.max(0, seuilMin));
        stats.put("seuilMax", seuilMax);
        return stats;
    }

    @Override
    public List<Map<String, Object>> pfesParEncadrant(Long versionId) {
        ImportVersion version = getVersion(versionId);
        List<PFE> pfes = pfeRepo.findByVersion(version);
        return pfes.stream()
                .filter(p -> p.getEncadrant() != null && p.getEncadrant().getProf() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getEncadrant().getProf().getNom()
                                + " " + p.getEncadrant().getProf().getPrenom(),
                        Collectors.counting())).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("nom",   e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }
    
    
    private List<SoutenanceView> loadSoutenances(SchedulingSolution sol, ImportVersion version) {
        // Priorité : solution en session (calcul récent non encore persisté ou déjà validé)
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

    @Override
    public List<Map<String, Object>> soutenancesParFiliere(SchedulingSolution sol,Long versionId) {
        ImportVersion version = getVersion(versionId);
        List<SoutenanceView> soutenances = loadSoutenances(sol, version);
        Map<String, Long> filMap = soutenances.stream()
        .collect(Collectors.groupingBy(SoutenanceView::filiere, Collectors.counting()));

        return filMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("filiere", e.getKey());
                    m.put("count",   e.getValue());
                    return m;
                })
                .sorted(Comparator.comparing(m -> (String) m.get("filiere")))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> soutenancesParProf(Long versionId) {
        ImportVersion version = getVersion(versionId);
        List<Jury> jurys = juryRepo.findAllWithRelations(version);
        Map<String, Integer> participations = new LinkedHashMap<>();
        for (Jury j : jurys) {
            if (j.getEncadrant() != null) inc(participations, nomProf(j.getEncadrant()));
            if (j.getProf1()     != null) inc(participations, nomProf(j.getProf1()));
            if (j.getProf2()     != null) inc(participations, nomProf(j.getProf2()));
        }
        return participations.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("nom",   e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> anomaliesEncadrement(Long versionId) {
        List<Map<String, Object>> anomaliesEncadr = new ArrayList<>();
        List<Map<String, Object>> pfeParEncadrant = pfesParEncadrant(versionId);
        double moyenne  = pfeParEncadrant.stream()
        .mapToLong(m -> (Long) m.get("count"))
        .average().orElse(0);
        int seuilMin = (int) Math.floor(moyenne) - 1;
        int seuilMax = (int) Math.ceil(moyenne)  + 1;
        for (Map<String, Object> entry : pfeParEncadrant) {
            long count = (Long) entry.get("count");
            if (count < seuilMin) {
                anomaliesEncadr.add(Map.of(
                        "prof",    entry.get("nom"),
                        "message", "Sous-chargé (" + count + " PFE(s) < seuil " + seuilMin + ")",
                        "type",    "sous"));
            } else if (count > seuilMax) {
                anomaliesEncadr.add(Map.of(
                        "prof",    entry.get("nom"),
                        "message", "Surchargé (" + count + " PFE(s) > seuil " + seuilMax + ")",
                        "type",    "sur"));
            }
        }
        return anomaliesEncadr;
    }


    
    

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

        @Override 
        public List<String> detecterAnomaliesPlanning(SchedulingSolution sol,Long versionId) {
        ImportVersion version = getVersion(versionId);
        List<SoutenanceView> soutenances = loadSoutenances(sol, version);
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

    // ─── Record interne ───────────────────────────────────────────────────
    private record SoutenanceView(
            Jury             jury,
            LocalDate        date,
            java.time.LocalTime heureDebut,
            java.time.LocalTime heureFin,
            String           filiere) {}
}