package org.sid.pfespring.constraints.soft;

import org.sid.pfespring.model.Jury;
import org.sid.pfespring.model.PFE;
import org.sid.pfespring.model.Prof;
import org.sid.pfespring.model.scheduling.PlannedSoutenance;
import org.sid.pfespring.model.scheduling.SchedulingSolution;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Objectif SOFT :
 * Préférer qu'au moins un membre du jury (hors encadrant) ait une spécialité
 * correspondant à la langue du PFE.
 *
 * SOFT uniquement — ne jamais bloquer si aucun prof de langue n'est disponible.
 * Score = proportion de PFEs dont le jury contient un prof de langue adéquat.
 */
@Component
public class PreferenceLangueObjective implements ObjectiveFunction {

    @Override
    public String getId() { return "preference_langue"; }

    @Override
    public double getWeight() { return 0.5; }

    @Override
    public double evaluate(SchedulingSolution solution) {
        List<PlannedSoutenance> planifiees = solution.getSoutenancesPlanifiees();
        if (planifiees.isEmpty()) return 1.0;

        long total = 0;
        long satisfaits = 0;

        for (PlannedSoutenance s : planifiees) {
            PFE pfe = s.getPfe();
            Jury jury = s.getJury();
            if (pfe == null || jury == null || pfe.getLangue() == null) continue;

            total++;
            String langue = pfe.getLangue().toUpperCase();

            // Vérifier prof1 et prof2 (pas l'encadrant)
            if (matchLangue(jury.getProf1(), langue) || matchLangue(jury.getProf2(), langue)) {
                satisfaits++;
            }
        }

        return total == 0 ? 1.0 : (double) satisfaits / total;
    }

    @Override
    public String explain(SchedulingSolution solution, double score) {
        return String.format(
                "Préférence langue : score %.0f%%. "
                        + "%.0f%% des jurys incluent un prof dont la spécialité correspond à la langue du PFE.",
                score * 100, score * 100
        );
    }

    private boolean matchLangue(Prof prof, String langue) {
        return prof != null
                && prof.getSpecialite() != null
                && prof.getSpecialite().toUpperCase().equals(langue);
    }
}