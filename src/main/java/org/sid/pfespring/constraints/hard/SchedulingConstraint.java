package org.sid.pfespring.constraints.hard;

import org.sid.pfespring.model.scheduling.ConstraintViolation;
import org.sid.pfespring.model.scheduling.PlannedSoutenance;
import org.sid.pfespring.model.scheduling.SchedulingContext;

import java.util.Optional;

/**
 * Interface racine pour toutes les contraintes HARD du moteur.
 *
 * Principe OCP :
 * - Ajouter une contrainte = créer une nouvelle classe qui implémente cette interface.
 * - Aucune modification du moteur ou des classes existantes.
 *
 * Le moteur découvre les contraintes via {@link ConstraintRegistry}.
 */
public interface SchedulingConstraint {

    /**
     * Identifiant unique de la contrainte (correspond à l'id dans scheduling-rules.yml).
     */
    String getId();

    /**
     * Vérifie si la soutenance candidate peut être placée dans le contexte courant.
     *
     * @param context    contexte complet (données + déjà planifiés)
     * @param candidate  soutenance en cours de placement
     * @return Optional vide si OK, Optional avec la violation si KO
     */
    Optional<ConstraintViolation> verify(
            SchedulingContext context,
            PlannedSoutenance candidate,
            java.util.List<PlannedSoutenance> dejaPlannifiees
    );

    /**
     * true = la contrainte est active (lu depuis le registre).
     * Permet de désactiver une contrainte sans supprimer sa classe.
     */
    default boolean isActive() {
        return true;
    }
}