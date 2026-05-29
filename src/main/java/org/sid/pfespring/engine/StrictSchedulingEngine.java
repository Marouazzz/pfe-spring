package org.sid.pfespring.engine;

import org.sid.pfespring.constraints.hard.SchedulingConstraint;
import org.sid.pfespring.model.Jury;
import org.sid.pfespring.model.Salle;
import org.sid.pfespring.model.scheduling.*;
import org.sid.pfespring.services.scheduling.ConstraintRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Algorithme STRICT — placement séquentiel greedy.
 *
 * Pour chaque jury :
 * 1. Parcourir les slots disponibles
 * 2. Pour chaque slot, tester les salles
 * 3. Si une salle est occupée → essayer la suivante
 * 4. Dès qu'une combinaison valide est trouvée → placer la soutenance
 *
 * Les contraintes HARD bloquent uniquement la combinaison courante,
 * mais n'arrêtent jamais la recherche globale.
 */
@Component
public class StrictSchedulingEngine {

    private final ConstraintRegistry registry;

    public StrictSchedulingEngine(ConstraintRegistry registry) {
        this.registry = registry;
    }

    public SchedulingSolution planifier(SchedulingContext context) {

        long debut = System.currentTimeMillis();

        List<PlannedSoutenance> result = new ArrayList<>();
        List<ConstraintViolation> softViol = new ArrayList<>();
        List<String> justifications = new ArrayList<>();

        List<SchedulingConstraint> hardConstraints =
                registry.getActiveHardConstraints();

        // ─────────────────────────────────────────────
        // Parcours des jurys
        // ─────────────────────────────────────────────
        for (Jury jury : context.getJurys()) {

            boolean place = false;

            // On teste d'abord les slots
            for (TimeSlot slot : context.getSlotsDisponibles()) {

                // Puis toutes les salles pour ce slot
                for (Salle salle : context.getSalles()) {

                    PlannedSoutenance candidat = PlannedSoutenance.builder()
                            .pfe(jury.getPfe())
                            .jury(jury)
                            .slot(slot)
                            .salle(salle)
                            .build();

                    boolean valide = true;

                    // ─────────────────────────────────
                    // Vérification contraintes HARD
                    // ─────────────────────────────────
                    for (SchedulingConstraint contrainte : hardConstraints) {

                        Optional<ConstraintViolation> violation =
                                contrainte.verify(context, candidat, result);

                        if (violation.isPresent()) {

                            valide = false;

                            // IMPORTANT :
                            // On NE break PAS les boucles de salles/slots.
                            // Cette combinaison est invalide,
                            // donc on teste simplement la suivante.
                            break;
                        }
                    }

                    // ─────────────────────────────────
                    // Si valide → placement
                    // ─────────────────────────────────
                    if (valide) {

                        PlannedSoutenance placed =
                                PlannedSoutenance.builder()
                                        .pfe(jury.getPfe())
                                        .jury(jury)
                                        .slot(slot)
                                        .salle(salle)
                                        .justification(
                                                "Placé en "
                                                        + slot
                                                        + " — salle "
                                                        + salle.getNomSalle())
                                        .build();

                        result.add(placed);

                        justifications.add(
                                "PFE#"
                                        + jury.getPfe().getId()
                                        + " → "
                                        + slot
                                        + " / "
                                        + salle.getNomSalle());

                        place = true;

                        // Salle trouvée → sortir uniquement
                        // des boucles slot/salle pour ce jury
                        break;
                    }
                }

                // Si placé → inutile de tester autres slots
                if (place) {
                    break;
                }
            }

            // ─────────────────────────────────────────
            // Aucun placement possible
            // ─────────────────────────────────────────
            if (!place) {

                PlannedSoutenance nonPlace =
                        PlannedSoutenance.builder()
                                .pfe(jury.getPfe())
                                .jury(jury)
                                .nonPlanifiee(true)
                                .raisonNonPlacement(
                                        "Aucun créneau valide trouvé "
                                                + "— conflit insoluble en mode strict.")
                                .build();

                result.add(nonPlace);

                justifications.add(
                        "PFE#"
                                + jury.getPfe().getId()
                                + " → NON PLANIFIÉ");
            }
        }

        long duree = System.currentTimeMillis() - debut;

        return buildSolution(
                result,
                softViol,
                justifications,
                duree);
    }

    // ─────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────

    private SchedulingSolution buildSolution(
            List<PlannedSoutenance> soutenances,
            List<ConstraintViolation> softViolations,
            List<String> justifications,
            long dureeMs) {

        long nonPlanifiees = soutenances.stream()
                .filter(PlannedSoutenance::isNonPlanifiee)
                .count();

        SchedulingSolution.Status status =
                nonPlanifiees == 0
                        ? SchedulingSolution.Status.COMPLETE
                        : (soutenances.size() == nonPlanifiees
                        ? SchedulingSolution.Status.VIDE
                        : SchedulingSolution.Status.PARTIELLE);

        return SchedulingSolution.builder()
                .status(status)
                .soutenances(soutenances)
                .violationsSoft(softViolations)
                .score(
                        SolutionScore.builder()
                                .global(0.0)
                                .parCritere(java.util.Collections.emptyMap())
                                .planifiees(
                                        (int) (soutenances.size() - nonPlanifiees))
                                .nonPlanifiees((int) nonPlanifiees)
                                .build())
                .algorithme("STRICT")
                .dureeCalculMs(dureeMs)
                .justifications(justifications)
                .build();
    }
}