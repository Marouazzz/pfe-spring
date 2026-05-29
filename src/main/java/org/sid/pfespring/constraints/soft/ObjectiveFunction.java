package org.sid.pfespring.constraints.soft;

import org.sid.pfespring.model.scheduling.SchedulingSolution;

/**
 * Interface pour les fonctions objectif (contraintes SOFT).
 *
 * Une fonction objectif évalue la qualité d'une solution selon un critère.
 * Score ∈ [0.0, 1.0] — 1.0 = critère parfaitement satisfait.
 *
 * Principe OCP : ajouter un critère = créer une nouvelle implémentation.
 * Aucune modification du moteur.
 */
public interface ObjectiveFunction {

    String getId();

    double getWeight();

    /**
     * Évalue la solution sur ce critère.
     * @return score ∈ [0.0, 1.0]
     */
    double evaluate(SchedulingSolution solution);

    /**
     * Explication humainement lisible du score obtenu.
     */
    String explain(SchedulingSolution solution, double score);

    default boolean isActive() { return true; }
}