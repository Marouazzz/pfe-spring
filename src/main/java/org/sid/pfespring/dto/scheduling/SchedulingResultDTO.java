package org.sid.pfespring.dto.scheduling;

import lombok.Builder;
import lombok.Getter;
import org.sid.pfespring.model.scheduling.ComparisonReport;
import org.sid.pfespring.model.scheduling.SchedulingSolution;

/**
 * Encapsule les résultats renvoyés au contrôleur puis à la vue.
 */
@Getter
@Builder
public class SchedulingResultDTO {

    /** Planning strict — peut être partiel si conflits insolubles. */
    private final SchedulingSolution strict;

    /** Planning optimisé — null si stratégie = STRICT uniquement. */
    private final SchedulingSolution optimise;

    /** Rapport de comparaison — null si une seule stratégie demandée. */
    private final ComparisonReport rapport;

    /** true si le strict a des soutenances non planifiées. */
    private final boolean hasConflicts;

    /** Message d'alerte si conflits détectés. */
    private final String alerteMessage;
}