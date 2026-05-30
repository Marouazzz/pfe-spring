package org.sid.pfespring.model.scheduling;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Rapport de comparaison entre la solution stricte et la solution optimisée.
 * Produit par {@link org.sid.pfespring.engine.JustificationEngine}.
 */
@Getter
@Builder
public class ComparisonReport {

    private final SolutionScore scoreStrict;
    private final SolutionScore scoreOptimise;

    /** Améliorations constatées entre strict et optimisé. */
    private final List<Improvement> ameliorations;

    /** Conclusion générale lisible. */
    private final String conclusion;

    /**
     * Représente l'amélioration d'un critère spécifique.
     */
    @Getter
    @Builder
    public static class Improvement {
        private final String critere;
        private final double avant;
        private final double apres;
        private final String explication;

        public double deltaPourcent() {
            if (avant == 0) return apres > 0 ? 100.0 : 0.0;
            return ((apres - avant) / avant) * 100.0;
        }

        public boolean estAmelioration() {
            return apres > avant;
        }
    }
}