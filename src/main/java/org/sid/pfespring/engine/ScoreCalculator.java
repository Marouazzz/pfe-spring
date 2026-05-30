package org.sid.pfespring.engine;

import org.sid.pfespring.constraints.soft.ObjectiveFunction;
import org.sid.pfespring.model.scheduling.SolutionScore;
import org.sid.pfespring.model.scheduling.SchedulingSolution;
import org.sid.pfespring.services.scheduling.ConstraintRegistry;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcule et attache le {@link SolutionScore} à une solution.
 *
 * Le score global est la moyenne pondérée des objectifs soft actifs.
 * Chaque critère retourne un score ∈ [0.0, 1.0].
 */
@Component
public class ScoreCalculator {

    private final ConstraintRegistry registry;

    public ScoreCalculator(ConstraintRegistry registry) {
        this.registry = registry;
    }

    /**
     * Retourne un nouveau {@link SolutionScore} calculé pour la solution donnée.
     * Le score taux de couverture est intégré dans planifiees / nonPlanifiees.
     */
    public SolutionScore calculer(SchedulingSolution solution) {

        List<ObjectiveFunction> objectives = registry.getActiveObjectives();

        Map<String, Double> parCritere = new LinkedHashMap<>();
        double totalWeight = 0;
        double weightedSum = 0;

        for (ObjectiveFunction obj : objectives) {
            double score = obj.evaluate(solution);
            parCritere.put(obj.getId(), score);
            totalWeight  += obj.getWeight();
            weightedSum  += obj.getWeight() * score;
        }

        double global = totalWeight > 0 ? weightedSum / totalWeight : 1.0;

        long planifiees    = solution.getSoutenancesPlanifiees().size();
        long nonPlanifiees = solution.getSoutenancesEnConflit().size();

        // Pénaliser le score global si solution partielle
        double couverture = (planifiees + nonPlanifiees) > 0
                ? (double) planifiees / (planifiees + nonPlanifiees) : 1.0;
        global = global * couverture;

        return SolutionScore.builder()
                .global(global)
                .parCritere(parCritere)
                .planifiees((int) planifiees)
                .nonPlanifiees((int) nonPlanifiees)
                .build();
    }

    /**
     * Génère les explications textuelles de chaque critère.
     */
    public List<String> expliquer(SchedulingSolution solution) {
        List<ObjectiveFunction> objectives = registry.getActiveObjectives();
        return objectives.stream()
                .map(obj -> obj.explain(solution, obj.evaluate(solution)))
                .toList();
    }
}
