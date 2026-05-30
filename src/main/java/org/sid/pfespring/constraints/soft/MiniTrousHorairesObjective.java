package org.sid.pfespring.constraints.soft;

import org.sid.pfespring.model.scheduling.PlannedSoutenance;
import org.sid.pfespring.model.scheduling.SchedulingSolution;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Objectif SOFT :
 * Minimiser les trous horaires dans les journées.
 *
 * Un trou est un intervalle libre entre deux soutenances dans la même salle
 * au cours de la même journée.
 *
 * Score = 1 - (taux de trous par rapport au temps total utilisé).
 */
@Component
public class MiniTrousHorairesObjective implements ObjectiveFunction {

    @Override
    public String getId() { return "minimiser_trous_horaires"; }

    @Override
    public double getWeight() { return 0.8; }

    @Override
    public double evaluate(SchedulingSolution solution) {
        List<PlannedSoutenance> planifiees = solution.getSoutenancesPlanifiees();
        if (planifiees.isEmpty()) return 1.0;

        // Grouper par salle + jour
        Map<String, List<PlannedSoutenance>> parSalleJour = planifiees.stream()
                .filter(s -> s.getSlot() != null && s.getSalle() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getSalle().getId() + "_" + s.getSlot().getDate()
                ));

        long totalMinutesUtiles = 0;
        long totalTrous = 0;

        for (List<PlannedSoutenance> groupe : parSalleJour.values()) {
            List<PlannedSoutenance> tri = groupe.stream()
                    .sorted(Comparator.comparing(s -> s.getSlot().getHeureDebut()))
                    .toList();

            for (int i = 0; i < tri.size() - 1; i++) {
                long trou = tri.get(i).getSlot().minutesJusquA(tri.get(i + 1).getSlot());
                if (trou > 0) totalTrous += trou;
            }

            long duree = tri.stream()
                    .mapToLong(s -> java.time.Duration.between(
                            s.getSlot().getHeureDebut(), s.getSlot().getHeureFin()).toMinutes())
                    .sum();
            totalMinutesUtiles += duree;
        }

        if (totalMinutesUtiles == 0) return 1.0;
        double ratioTrous = (double) totalTrous / (totalMinutesUtiles + totalTrous);
        return Math.max(0.0, 1.0 - ratioTrous);
    }

    @Override
    public String explain(SchedulingSolution solution, double score) {
        return String.format(
                "Trous horaires : score %.0f%%. %s",
                score * 100,
                score >= 0.8
                        ? "Les journées sont bien compactes."
                        : "Des trous horaires importants subsistent dans le planning."
        );
    }
}