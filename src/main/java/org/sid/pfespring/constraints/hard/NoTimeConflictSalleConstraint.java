package org.sid.pfespring.constraints.hard;

import org.sid.pfespring.model.scheduling.ConstraintViolation;
import org.sid.pfespring.model.scheduling.PlannedSoutenance;
import org.sid.pfespring.model.scheduling.SchedulingContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Contrainte HARD :
 * Une salle ne peut accueillir qu'une seule soutenance par créneau.
 */
@Component
public class NoTimeConflictSalleConstraint implements SchedulingConstraint {

    @Override
    public String getId() { return "no_time_conflict_salle"; }

    @Override
    public Optional<ConstraintViolation> verify(
            SchedulingContext context,
            PlannedSoutenance candidate,
            List<PlannedSoutenance> dejaPlannifiees) {

        if (candidate.getSlot() == null || candidate.getSalle() == null)
            return Optional.empty();

        for (PlannedSoutenance p : dejaPlannifiees) {
            if (p.isNonPlanifiee() || p.getSlot() == null || p.getSalle() == null) continue;

            boolean memeSalle = p.getSalle().getId().equals(candidate.getSalle().getId());
            boolean chevauchement = candidate.getSlot().chevauchePare(p.getSlot());

            if (memeSalle && chevauchement) {
                return Optional.of(ConstraintViolation.builder()
                        .constraintId(getId())
                        .niveau(ConstraintViolation.Niveau.HARD)
                        .description("Salle " + candidate.getSalle().getNomSalle()
                                + " déjà occupée le " + p.getSlot())
                        .soutenanceConcernee(candidate)
                        .build());
            }
        }
        return Optional.empty();
    }
}