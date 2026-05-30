package org.sid.pfespring.constraints.hard;

import org.sid.pfespring.model.scheduling.ConstraintViolation;
import org.sid.pfespring.model.scheduling.PlannedSoutenance;
import org.sid.pfespring.model.scheduling.SchedulingContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Contrainte HARD :
 * Un jury doit comporter au minimum {@code juryMembresMinimum} membres
 * (encadrant + prof1 + prof2).
 *
 * En pratique le nombre minimum est 3 (config YAML).
 * Cette contrainte bloque le placement si le jury est incomplet.
 */
@Component
public class JuryMinMembresConstraint implements SchedulingConstraint {

    @Override
    public String getId() {
        return "jury_min_membres";
    }

    @Override
    public Optional<ConstraintViolation> verify(
            SchedulingContext context,
            PlannedSoutenance candidate,
            List<PlannedSoutenance> dejaPlannifiees) {

        if (candidate.getJury() == null) {
            return Optional.of(ConstraintViolation.builder()
                    .constraintId(getId())
                    .niveau(ConstraintViolation.Niveau.HARD)
                    .description("Jury absent pour PFE#"
                            + (candidate.getPfe() != null ? candidate.getPfe().getId() : "?"))
                    .soutenanceConcernee(candidate)
                    .build());
        }

        int membresPresents = 0;
        if (candidate.getJury().getEncadrant() != null) membresPresents++;
        if (candidate.getJury().getProf1()     != null) membresPresents++;
        if (candidate.getJury().getProf2()     != null) membresPresents++;

        int minimum = context.getConfig().getJuryMembresMinimum();

        if (membresPresents < minimum) {
            return Optional.of(ConstraintViolation.builder()
                    .constraintId(getId())
                    .niveau(ConstraintViolation.Niveau.HARD)
                    .description("Jury incomplet : " + membresPresents
                            + " membre(s) présent(s) / " + minimum + " requis")
                    .soutenanceConcernee(candidate)
                    .build());
        }

        return Optional.empty();
    }
}
