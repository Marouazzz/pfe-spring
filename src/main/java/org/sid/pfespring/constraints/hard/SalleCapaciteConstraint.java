package org.sid.pfespring.constraints.hard;

import org.sid.pfespring.model.scheduling.ConstraintViolation;
import org.sid.pfespring.model.scheduling.PlannedSoutenance;
import org.sid.pfespring.model.scheduling.SchedulingContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Contrainte HARD :
 * La capacité de la salle doit être >= nombre d'étudiants du PFE.
 */
@Component
public class SalleCapaciteConstraint implements SchedulingConstraint {

    @Override
    public String getId() { return "salle_capacite"; }

    @Override
    public Optional<ConstraintViolation> verify(
            SchedulingContext context,
            PlannedSoutenance candidate,
            List<PlannedSoutenance> dejaPlannifiees) {

        if (!context.getConfig().isVerifierCapaciteSalle()) return Optional.empty();
        if (candidate.getSalle() == null || candidate.getPfe() == null) return Optional.empty();

        int nbEtudiants = candidate.getPfe().getEtudiants() != null
                ? candidate.getPfe().getEtudiants().size() : 0;
        int capacite = candidate.getSalle().getCapacite();

        if (nbEtudiants > capacite) {
            return Optional.of(ConstraintViolation.builder()
                    .constraintId(getId())
                    .niveau(ConstraintViolation.Niveau.HARD)
                    .description("Salle " + candidate.getSalle().getNomSalle()
                            + " (capacité " + capacite + ") insuffisante pour "
                            + nbEtudiants + " étudiant(s)")
                    .soutenanceConcernee(candidate)
                    .build());
        }
        return Optional.empty();
    }
}