package org.sid.pfespring.model.scheduling;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Score global d'une solution + détail par critère.
 * Score ∈ [0.0, 1.0] — 1.0 = solution parfaite.
 */
@Getter
@Builder
public class SolutionScore {

    /** Score global pondéré. */
    private final double global;

    /** Score détaillé par ID d'objectif soft. */
    private final Map<String, Double> parCritere;

    /** Nombre de soutenances planifiées. */
    private final int planifiees;

    /** Nombre de soutenances non planifiées (conflits insolubles). */
    private final int nonPlanifiees;

    /** Taux de couverture : planifiees / (planifiees + nonPlanifiees). */
    public double tauxCouverture() {
        int total = planifiees + nonPlanifiees;
        return total == 0 ? 0.0 : (double) planifiees / total;
    }
}