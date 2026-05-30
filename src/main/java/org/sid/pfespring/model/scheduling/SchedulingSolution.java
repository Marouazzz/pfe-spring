package org.sid.pfespring.model.scheduling;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Résultat produit par un algorithme de planification.
 *
 * Une solution peut être :
 * - COMPLETE  : toutes les soutenances sont planifiées
 * - PARTIELLE : certaines soutenances n'ont pas pu être placées
 * - VIDE      : aucune soutenance planifiée (erreur de données)
 */
@Getter
@Builder
public class SchedulingSolution {

    public enum Status { COMPLETE, PARTIELLE, VIDE }

    private final Status status;

    /** Toutes les soutenances planifiées + non planifiées. */
    private final List<PlannedSoutenance> soutenances;

    /** Violations soft détectées (informatif). */
    private final List<ConstraintViolation> violationsSoft;

    /** Score calculé. */
    private final SolutionScore score;

    /** Nom de l'algorithme qui a produit cette solution. */
    private final String algorithme;     // "STRICT" ou "OPTIMISE"

    /** Durée de calcul en millisecondes. */
    private final long dureeCalculMs;

    /** Justifications lisibles des décisions principales. */
    private final List<String> justifications;

    /** Message d'alerte si solution partielle. */
    public String getAlerteMessage() {
        if (status == Status.PARTIELLE) {
            long nonPlanifiees = soutenances.stream()
                    .filter(PlannedSoutenance::isNonPlanifiee)
                    .count();
            return nonPlanifiees + " soutenance(s) n'ont pas pu être planifiées "
                    + "en raison de conflits insolubles. "
                    + "Consultez le rapport pour le détail. "
                    + "Le mode optimisé peut proposer une meilleure solution.";
        }
        return null;
    }

    /** Raccourci — soutenances effectivement planifiées. */
    public List<PlannedSoutenance> getSoutenancesPlanifiees() {
        return soutenances.stream()
                .filter(s -> !s.isNonPlanifiee())
                .toList();
    }

    /** Raccourci — soutenances en conflit. */
    public List<PlannedSoutenance> getSoutenancesEnConflit() {
        return soutenances.stream()
                .filter(PlannedSoutenance::isNonPlanifiee)
                .toList();
    }
}