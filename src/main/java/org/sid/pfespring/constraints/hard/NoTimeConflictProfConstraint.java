package org.sid.pfespring.constraints.hard;

import org.sid.pfespring.model.Prof;
import org.sid.pfespring.model.scheduling.ConstraintViolation;
import org.sid.pfespring.model.scheduling.PlannedSoutenance;
import org.sid.pfespring.model.scheduling.SchedulingContext;
import org.sid.pfespring.model.scheduling.TimeSlot;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contrainte HARD :
 * 1. Un professeur ne peut pas être dans deux jurys qui se chevauchent.
 * 2. Un professeur doit avoir une pause minimale (config) entre deux jurys
 *    dans la même journée.
 */
@Component
public class NoTimeConflictProfConstraint implements SchedulingConstraint {

    @Override
    public String getId() {
        return "no_time_conflict_prof";
    }

    @Override
    public Optional<ConstraintViolation> verify(
            SchedulingContext context,
            PlannedSoutenance candidate,
            List<PlannedSoutenance> dejaPlannifiees) {

        if (candidate.getSlot() == null) return Optional.empty();

        TimeSlot slotCandidat = candidate.getSlot();
        int pauseMin = context.getConfig().getPauseEntreProfMinutes();

        // Extraire tous les profs du jury candidat
        Set<Long> profsCandidat = getProfsIds(candidate);

        for (PlannedSoutenance planifiee : dejaPlannifiees) {
            if (planifiee.getSlot() == null || planifiee.isNonPlanifiee()) continue;

            Set<Long> profsPlannifiee = getProfsIds(planifiee);

            // Intersection des profs
            Set<Long> communs = profsCandidat.stream()
                    .filter(profsPlannifiee::contains)
                    .collect(Collectors.toSet());

            if (communs.isEmpty()) continue;

            TimeSlot slotPlannifie = planifiee.getSlot();

            // Vérif chevauchement direct
            if (slotCandidat.chevauchePare(slotPlannifie)) {
                String nomProf = getNomProf(candidate, communs.iterator().next());
                return Optional.of(ConstraintViolation.builder()
                        .constraintId(getId())
                        .niveau(ConstraintViolation.Niveau.HARD)
                        .description("Conflit horaire : Prof " + nomProf
                                + " est déjà en jury le " + slotPlannifie)
                        .soutenanceConcernee(candidate)
                        .build());
            }

            // Vérif pause minimale (même journée)
            if (slotCandidat.getDate().equals(slotPlannifie.getDate()) && pauseMin > 0) {
                long pause1 = slotPlannifie.minutesJusquA(slotCandidat); // fin planifié → début candidat
                long pause2 = slotCandidat.minutesJusquA(slotPlannifie); // fin candidat → début planifié
                long pauseEffective = Math.max(pause1, pause2);

                if (pauseEffective >= 0 && pauseEffective < pauseMin) {
                    String nomProf = getNomProf(candidate, communs.iterator().next());
                    return Optional.of(ConstraintViolation.builder()
                            .constraintId(getId())
                            .niveau(ConstraintViolation.Niveau.HARD)
                            .description("Pause insuffisante pour Prof " + nomProf
                                    + " : " + pauseEffective + " min < " + pauseMin + " min requis")
                            .soutenanceConcernee(candidate)
                            .build());
                }
            }
        }

        return Optional.empty();
    }

    private Set<Long> getProfsIds(PlannedSoutenance s) {
        Set<Long> ids = new java.util.HashSet<>();
        if (s.getJury() == null) return ids;
        if (s.getJury().getEncadrant() != null) ids.add(s.getJury().getEncadrant().getId());
        if (s.getJury().getProf1() != null)     ids.add(s.getJury().getProf1().getId());
        if (s.getJury().getProf2() != null)     ids.add(s.getJury().getProf2().getId());
        return ids;
    }

    private String getNomProf(PlannedSoutenance s, Long profId) {
        if (s.getJury() == null) return "inconnu";
        for (Prof p : List.of(
                s.getJury().getEncadrant(),
                s.getJury().getProf1(),
                s.getJury().getProf2())) {
            if (p != null && p.getId().equals(profId))
                return p.getNom() + " " + p.getPrenom();
        }
        return "inconnu";
    }
}