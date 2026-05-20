package org.sid.pfespring.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sid.pfespring.model.Encadrant;
import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.model.Jury;
import org.sid.pfespring.model.Soutenance;
import org.sid.pfespring.repository.EncadrantRepository;
import org.sid.pfespring.repository.ImportVersionRepository;
import org.sid.pfespring.repository.JuryRepository;
import org.sid.pfespring.repository.SoutenanceRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final EncadrantRepository encadrantRepo;
    private final JuryRepository juryRepo;
    private final SoutenanceRepository soutenanceRepo;
    private final ImportVersionRepository versionRepo;

    public DashboardServiceImpl(EncadrantRepository encadrantRepo,
                                JuryRepository juryRepo,
                                SoutenanceRepository soutenanceRepo,
                                ImportVersionRepository versionRepo) {
        this.encadrantRepo = encadrantRepo;
        this.juryRepo = juryRepo;
        this.soutenanceRepo = soutenanceRepo;
        this.versionRepo = versionRepo;
    }

    private ImportVersion getVersion(Long versionId) {
        return versionRepo.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version introuvable : " + versionId));
    }

    @Override
    public List<Map<String, Object>> pfesParEncadrant(Long versionId) {
        ImportVersion version = getVersion(versionId);
        List<Encadrant> encadrants = encadrantRepo.findByVersion(version);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Encadrant enc : encadrants) {

            Map<String, Object> entry = new HashMap<>();
            entry.put("nom",   enc.getProf().getNom() + " " + enc.getProf().getPrenom());
            entry.put("count", enc.getPfes().size()); // depuis BDD
            result.add(entry);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> soutenancesParProf(Long versionId) {
        ImportVersion version = getVersion(versionId);
        List<Jury> jurys = juryRepo.findAllWithRelations(version);
        Map<String, Integer> counts = new HashMap<>();
        for (Jury j : jurys) {
            if (j.getEncadrant() != null) {
                String k = j.getEncadrant().getNom() + " " + j.getEncadrant().getPrenom();
                counts.merge(k, 1, Integer::sum);
            }
            if (j.getProf1() != null) {
                String k = j.getProf1().getNom() + " " + j.getProf1().getPrenom();
                counts.merge(k, 1, Integer::sum);
            }
            if (j.getProf2() != null) {
                String k = j.getProf2().getNom() + " " + j.getProf2().getPrenom();
                counts.merge(k, 1, Integer::sum);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        counts.forEach((nom, cnt) -> {
            Map<String, Object> e = new HashMap<>();
            e.put("nom", nom);
            e.put("count", cnt);
            result.add(e);
        });
        return result;
    }

    @Override
    public List<Map<String, Object>> soutenancesParFiliere(Long versionId) {
        ImportVersion version = getVersion(versionId);
        List<Soutenance> soutenances = soutenanceRepo
                .findByVersionOrderByDateSoutenanceAscHeureDebutAscSalleNomSalleAsc(version);
        Map<String, Integer> counts = new HashMap<>();
        for (Soutenance s : soutenances) {
            String filiere = s.getPfe().getFiliere() != null
                    ? s.getPfe().getFiliere().name() : "Inconnue";
            counts.merge(filiere, 1, Integer::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        counts.forEach((f, cnt) -> {
            Map<String, Object> e = new HashMap<>();
            e.put("filiere", f);
            e.put("count", cnt);
            result.add(e);
        });
        return result;
    }

    @Override
    public Map<String, Long> statsGlobales(Long versionId) {
        ImportVersion version = getVersion(versionId);
        Map<String, Long> stats = new HashMap<>();

        List<Encadrant> encadrants = encadrantRepo.findByVersionWithPfes(version);
        // Nombre d'encadrants
        stats.put("totalProfs", (long) encadrants.size());

        // Nombre de PFEs total
        int totalPfesInt = encadrants.stream()
                .mapToInt(e -> e.getPfes() != null ? e.getPfes().size() : 0)
                .sum();
        stats.put("totalPfes", (long) totalPfesInt);

        // Seuils dynamiques
        if (!encadrants.isEmpty()) {
            int capMin = totalPfesInt / encadrants.size();
            stats.put("seuilMin", (long) capMin);
            stats.put("seuilMax", (long)(capMin + 1));
        }

        // Soutenances
        List<Soutenance> soutenances = soutenanceRepo
                .findByVersionOrderByDateSoutenanceAscHeureDebutAscSalleNomSalleAsc(version);
        stats.put("totalSoutenances", (long) soutenances.size());

        // Salles distinctes
        long sallesDistinctes = soutenances.stream()
                .map(s -> s.getSalle().getId())
                .distinct().count();
        stats.put("totalSalles", sallesDistinctes);
        //pour nbre de jour de soutnace
        long joursDistincts = soutenances.stream()
                .map(s -> s.getDateSoutenance())
                .distinct().count();
        stats.put("totalJours", joursDistincts);

        return stats;
    }
    @Override
    public List<Map<String, Object>> anomaliesEncadrement(Long versionId) {
        ImportVersion version = getVersion(versionId);
        List<Encadrant> encadrants = encadrantRepo.findByVersionWithPfes(version);
        List<Map<String, Object>> anomalies = new ArrayList<>();

        if (encadrants.isEmpty()) return anomalies;

        // Recalculer les vrais seuils comme appliquerAffectation()
        int totalPfes      = encadrants.stream()
                .mapToInt(e -> e.getPfes() != null ? e.getPfes().size() : 0)
                .sum();
        int nbEncadrants   = encadrants.size();
        int capaciteMin    = totalPfes / nbEncadrants;
        int capaciteMax    = capaciteMin + 1;

        // Les 2 seules valeurs normales sont capaciteMin et capaciteMax
        for (Encadrant enc : encadrants) {
            if (enc.getPfes() == null || enc.getPfes().isEmpty()) continue;
            int count = enc.getPfes().size();
            String nom = enc.getProf().getNom() + " " + enc.getProf().getPrenom();

            if (count < capaciteMin) {
                // Impossible normalement — mais on détecte quand même
                Map<String, Object> a = new HashMap<>();
                a.put("prof",    nom);
                a.put("count",   count);
                a.put("type",    "SOUS");
                a.put("message", "Encadre " + count + " PFE(s) — attendu : " + capaciteMin + " ou " + capaciteMax);
                anomalies.add(a);
            } else if (count > capaciteMax) {
                Map<String, Object> a = new HashMap<>();
                a.put("prof",    nom);
                a.put("count",   count);
                a.put("type",    "SUR");
                a.put("message", "Encadre " + count + " PFEs — attendu : " + capaciteMin + " ou " + capaciteMax);
                anomalies.add(a);
            }
        }
        return anomalies;
    }
}