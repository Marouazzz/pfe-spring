package org.sid.pfespring.services.scheduling;

import org.sid.pfespring.constraints.hard.SchedulingConstraint;
import org.sid.pfespring.constraints.soft.ObjectiveFunction;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registre central des contraintes et objectifs.
 *
 * Spring injecte automatiquement TOUTES les implémentations de
 * {@link SchedulingConstraint} et {@link ObjectiveFunction} déclarées
 * avec @Component dans l'application.
 *
 * Principe OCP :
 * - Ajouter une contrainte = créer une classe + @Component.
 * - Aucune modification de ce registre.
 * - Aucune modification du moteur.
 */
@Component
public class ConstraintRegistry {

    private final List<SchedulingConstraint> hardConstraints;
    private final List<ObjectiveFunction> objectives;

    public ConstraintRegistry(
            List<SchedulingConstraint> hardConstraints,
            List<ObjectiveFunction> objectives) {
        this.hardConstraints = hardConstraints;
        this.objectives      = objectives;
    }

    /** Contraintes hard actives uniquement. */
    public List<SchedulingConstraint> getActiveHardConstraints() {
        return hardConstraints.stream()
                .filter(SchedulingConstraint::isActive)
                .toList();
    }

    /** Objectifs soft actifs uniquement. */
    public List<ObjectiveFunction> getActiveObjectives() {
        return objectives.stream()
                .filter(ObjectiveFunction::isActive)
                .toList();
    }
}