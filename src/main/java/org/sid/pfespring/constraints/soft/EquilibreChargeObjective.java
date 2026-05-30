package org.sid.pfespring.constraints.soft;

import org.sid.pfespring.model.scheduling.PlannedSoutenance;
import org.sid.pfespring.model.scheduling.SchedulingSolution;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Objectif SOFT :
 * Répartir équitablement la charge des jurys entre les professeurs.
 *
 * Score = 1 - (variance normalisée des charges).
 * Un score de 1.0 signifie que tous les profs ont exactement le même nombre de jurys.
 */
@Component
public class EquilibreChargeObjective implements ObjectiveFunction {

    @Override
    public String getId() { return "equilibre_charge_jury"; }

    @Override
    public double getWeight() { return 1.0; }

    @Override
    public double evaluate(SchedulingSolution solution) {
        Map<Long, Integer> chargeParProf = new HashMap<>();

        for (PlannedSoutenance s : solution.getSoutenancesPlanifiees()) {
            if (s.getJury() == null) continue;
            compte(chargeParProf, s.getJury().getEncadrant());
            compte(chargeParProf, s.getJury().getProf1());
            compte(chargeParProf, s.getJury().getProf2());
        }

        if (chargeParProf.isEmpty()) return 1.0;

        double moyenne = chargeParProf.values().stream()
                .mapToInt(Integer::intValue)
                .average().orElse(0);

        if (moyenne == 0) return 1.0;

        double variance = chargeParProf.values().stream()
                .mapToDouble(c -> Math.pow(c - moyenne, 2))
                .average().orElse(0);

        double ecartNormalise = Math.sqrt(variance) / moyenne;
        return Math.max(0.0, 1.0 - Math.min(ecartNormalise, 1.0));
    }

    @Override
    public String explain(SchedulingSolution solution, double score) {
        return String.format(
                "Équilibre de charge : score %.0f%%. %s",
                score * 100,
                score >= 0.8
                        ? "La charge est bien répartie entre les professeurs."
                        : "La charge est inégalement distribuée entre les professeurs."
        );
    }

    private void compte(Map<Long, Integer> map, org.sid.pfespring.model.Prof prof) {
        if (prof != null) map.merge(prof.getId(), 1, Integer::sum);
    }
}