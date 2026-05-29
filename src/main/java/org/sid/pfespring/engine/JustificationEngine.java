package org.sid.pfespring.engine;

import org.sid.pfespring.model.scheduling.ComparisonReport;
import org.sid.pfespring.model.scheduling.SchedulingSolution;
import org.sid.pfespring.model.scheduling.SolutionScore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Produit le {@link ComparisonReport} en comparant la solution STRICT
 * et la solution OPTIMISÉ.
 *
 * Appelé uniquement si les deux solutions sont disponibles (mode LES_DEUX).
 */
@Component
public class JustificationEngine {

    /**
     * Compare les deux solutions et produit un rapport lisible.
     *
     * @param strict    solution produite par l'algorithme strict
     * @param optimise  solution produite par le recuit simulé
     * @return rapport de comparaison
     */
    public ComparisonReport comparer(SchedulingSolution strict, SchedulingSolution optimise) {

        SolutionScore scoreStrict   = strict.getScore();
        SolutionScore scoreOptimise = optimise.getScore();

        List<ComparisonReport.Improvement> ameliorations = new ArrayList<>();


        ameliorations.add(ComparisonReport.Improvement.builder()
                .critere("Taux de couverture")
                .avant(scoreStrict.tauxCouverture())
                .apres(scoreOptimise.tauxCouverture())
                .explication(String.format(
                        "STRICT : %d planifiées / %d total — OPTIMISÉ : %d planifiées / %d total",
                        scoreStrict.getPlanifiees(),
                        scoreStrict.getPlanifiees() + scoreStrict.getNonPlanifiees(),
                        scoreOptimise.getPlanifiees(),
                        scoreOptimise.getPlanifiees() + scoreOptimise.getNonPlanifiees()))
                .build());

        //score
        ameliorations.add(ComparisonReport.Improvement.builder()
                .critere("Score global")
                .avant(scoreStrict.getGlobal())
                .apres(scoreOptimise.getGlobal())
                .explication(String.format(
                        "Score pondéré global : %.3f → %.3f",
                        scoreStrict.getGlobal(), scoreOptimise.getGlobal()))
                .build());

        // détail par critère soft
        Map<String, Double> criteresStrict   = scoreStrict.getParCritere();
        Map<String, Double> criteresOptimise = scoreOptimise.getParCritere();

        criteresOptimise.forEach((critere, apres) -> {
            double avant = criteresStrict.getOrDefault(critere, 0.0);
            ameliorations.add(ComparisonReport.Improvement.builder()
                    .critere(critere)
                    .avant(avant)
                    .apres(apres)
                    .explication(String.format(
                            "%s : %.0f%% → %.0f%%", critere, avant * 100, apres * 100))
                    .build());
        });


        String conclusion = buildConclusion(scoreStrict, scoreOptimise, ameliorations);

        return ComparisonReport.builder()
                .scoreStrict(scoreStrict)
                .scoreOptimise(scoreOptimise)
                .ameliorations(ameliorations)
                .conclusion(conclusion)
                .build();
    }

    private String buildConclusion(SolutionScore strict,
                                   SolutionScore optimise,
                                   List<ComparisonReport.Improvement> ameliorations) {

        long nbAmeliorations = ameliorations.stream()
                .filter(ComparisonReport.Improvement::estAmelioration)
                .count();

        if (optimise.getPlanifiees() > strict.getPlanifiees()) {
            return String.format(
                    " L'algorithme optimisé planifie %d soutenance(s) de plus que le mode strict. "
                    + "Score global amélioré de %.1f%% à %.1f%%.",
                    optimise.getPlanifiees() - strict.getPlanifiees(),
                    strict.getGlobal() * 100, optimise.getGlobal() * 100);
        }

        if (nbAmeliorations > 1) {
            return String.format(
                    " L'algorithme optimisé améliore %d critère(s) sur %d. "
                    + "Score global : %.1f%% → %.1f%%.",
                    nbAmeliorations, ameliorations.size(),
                    strict.getGlobal() * 100, optimise.getGlobal() * 100);
        }

        if (optimise.getGlobal() <= strict.getGlobal()) {
            return " Les deux algorithmes produisent des résultats équivalents pour ce jeu de données. "
                   + "Le mode strict est suffisant.";
        }

        return String.format(
                " L'algorithme optimisé améliore légèrement le score global (%.1f%% → %.1f%%).",
                strict.getGlobal() * 100, optimise.getGlobal() * 100);
    }
}
