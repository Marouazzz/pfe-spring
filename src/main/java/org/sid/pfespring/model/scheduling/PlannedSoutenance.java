package org.sid.pfespring.model.scheduling;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.sid.pfespring.model.Jury;
import org.sid.pfespring.model.PFE;
import org.sid.pfespring.model.Salle;

/**
 * Représente l'affectation d'un PFE à un créneau, une salle et un jury.
 *
 * C'est le résultat élémentaire produit par le moteur de planification.
 * Distinct de l'entité JPA {@link org.sid.pfespring.model.Soutenance} :
 * PlannedSoutenance existe en mémoire pendant le calcul et n'est
 * persisté que si le planning est validé.
 */
@Getter
@Setter
@Builder
public class PlannedSoutenance {

    private final PFE pfe;
    private final Jury jury;
    private final Salle salle;
    private TimeSlot slot;

    /**
     * true si cette soutenance n'a pas pu être planifiée
     * (conflit insoluble en mode strict).
     * Elle apparaîtra en rouge dans le rapport.
     */
    @Builder.Default
    private boolean nonPlanifiee = false;

    /** Raison du non-placement (pour le rapport). */
    private String raisonNonPlacement;

    /** Justification de la décision d'affectation (pour le rapport). */
    private String justification;

    public void setSalle(Salle salle) {
    }
}