package org.sid.pfespring.engine;

import org.sid.pfespring.constraints.hard.SchedulingConstraint;
import org.sid.pfespring.constraints.soft.ObjectiveFunction;
import org.sid.pfespring.model.Salle;
import org.sid.pfespring.model.scheduling.*;
import org.sid.pfespring.services.scheduling.ConstraintRegistry;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Algorithme OPTIMISÉ — Simulated Annealing.
 *
 * Version adaptée :
 * - Respect strict des contraintes HARD
 * - Si une salle est occupée → une autre est testée
 * - Aucun arrêt prématuré de recherche
 * - Les mouvements invalides sont simplement rejetés
 */
@Component
public class OptimisedSchedulingEngine {

    private final ConstraintRegistry registry;
    private final StrictSchedulingEngine strictEngine;

    public OptimisedSchedulingEngine(
            ConstraintRegistry registry,
            StrictSchedulingEngine strictEngine) {

        this.registry = registry;
        this.strictEngine = strictEngine;
    }

    public SchedulingSolution planifier(SchedulingContext context) {

        long debut = System.currentTimeMillis();

        // solution initial stict
        SchedulingSolution initial =
                strictEngine.planifier(context);

        if (initial.getStatus() == SchedulingSolution.Status.VIDE) {
            return initial;
        }

        // cuureent state
        List<PlannedSoutenance> current =
                deepCopy(initial.getSoutenances());

        double currentScore =
                evaluerScore(current, context);

        List<PlannedSoutenance> best =
                deepCopy(current);

        double bestScore = currentScore;

        // 3. Paramètres SA

        double temperature =
                context.getConfig()
                        .getOptimisationTemperatureInitiale();

        double cooling =
                context.getConfig()
                        .getOptimisationTauxRefroidissement();

        int iterMax =
                context.getConfig()
                        .getOptimisationIterationsMax();

        long tempsLimiteMs =
                context.getConfig()
                        .getOptimisationTempsLimiteSecondes()
                        * 1000L;

        Random rng = new Random(42);

        List<TimeSlot> slots =
                context.getSlotsDisponibles();

        List<Salle> salles =
                context.getSalles();

        // main loop
        for (int iter = 0; iter < iterMax; iter++) {

            // Stop temporel
            if (System.currentTimeMillis() - debut > tempsLimiteMs) {
                break;
            }

            // Génération voisin
            List<PlannedSoutenance> voisin =
                    deepCopy(current);

            appliquerMouvement(
                    voisin,
                    slots,
                    salles,
                    rng,
                    context);

            // HARD invalides → rejet
            if (!hardConstraintsOk(voisin, context)) {
                continue;
            }

            double voisinScore =
                    evaluerScore(voisin, context);

            double delta =
                    voisinScore - currentScore;

            // Critère Metropolis
            if (delta > 0
                    || rng.nextDouble() < Math.exp(delta / temperature)) {

                current = voisin;
                currentScore = voisinScore;

                if (currentScore > bestScore) {
                    best = deepCopy(current);
                    bestScore = currentScore;
                }
            }

            // Refroidissement
            temperature *= cooling;
        }

        long duree =
                System.currentTimeMillis() - debut;

        //solution finale
        long nonPlanifiees = best.stream()
                .filter(PlannedSoutenance::isNonPlanifiee)
                .count();

        SchedulingSolution.Status status =
                nonPlanifiees == 0
                        ? SchedulingSolution.Status.COMPLETE
                        : (best.size() == nonPlanifiees
                        ? SchedulingSolution.Status.VIDE
                        : SchedulingSolution.Status.PARTIELLE);

        return SchedulingSolution.builder()
                .status(status)
                .soutenances(best)
                .violationsSoft(Collections.emptyList())
                .score(
                        SolutionScore.builder()
                                .global(bestScore)
                                .parCritere(Collections.emptyMap())
                                .planifiees(
                                        (int) (best.size() - nonPlanifiees))
                                .nonPlanifiees((int) nonPlanifiees)
                                .build())
                .algorithme("OPTIMISE")
                .dureeCalculMs(duree)
                .justifications(List.of(
                        "Recuit simulé",
                        "Itérations max : " + iterMax,
                        "Score final : "
                                + String.format("%.3f", bestScore),
                        "Non planifiées : " + nonPlanifiees))
                .build();
    }

   //eval score

    private double evaluerScore(
            List<PlannedSoutenance> soutenances,
            SchedulingContext ctx) {

        SchedulingSolution sol =
                SchedulingSolution.builder()
                        .status(SchedulingSolution.Status.PARTIELLE)
                        .soutenances(soutenances)
                        .violationsSoft(Collections.emptyList())
                        .score(
                                SolutionScore.builder()
                                        .global(0)
                                        .parCritere(Collections.emptyMap())
                                        .planifiees(0)
                                        .nonPlanifiees(0)
                                        .build())
                        .algorithme("TMP")
                        .dureeCalculMs(0)
                        .justifications(Collections.emptyList())
                        .build();

        List<ObjectiveFunction> objectives =
                registry.getActiveObjectives();

        if (objectives.isEmpty()) {
            return 1.0;
        }

        double totalWeight =
                objectives.stream()
                        .mapToDouble(ObjectiveFunction::getWeight)
                        .sum();

        double score = 0;

        for (ObjectiveFunction obj : objectives) {
            score += obj.getWeight() * obj.evaluate(sol);
        }

        return totalWeight > 0
                ? score / totalWeight
                : 0;
    }

    // Vérif HARD


    private boolean hardConstraintsOk(
            List<PlannedSoutenance> soutenances,
            SchedulingContext ctx) {

        List<SchedulingConstraint> constraints =
                registry.getActiveHardConstraints();

        List<PlannedSoutenance> placed =
                new ArrayList<>();

        for (PlannedSoutenance s : soutenances) {

            if (s.isNonPlanifiee()) {
                continue;
            }

            for (SchedulingConstraint c : constraints) {

                if (c.verify(ctx, s, placed).isPresent()) {


                    // si salle occupée ou conflit rejet simplement CE voisin
                    return false;
                }
            }

            placed.add(s);
        }

        return true;
    }



    private void appliquerMouvement(
            List<PlannedSoutenance> soutenances,
            List<TimeSlot> slots,
            List<Salle> salles,
            Random rng,
            SchedulingContext context) {

        List<PlannedSoutenance> planifiees =
                soutenances.stream()
                        .filter(s -> !s.isNonPlanifiee())
                        .toList();

        if (planifiees.isEmpty()) {
            return;
        }

        int type = rng.nextInt(3);

        // swaping les slots
        if (type == 0 && planifiees.size() >= 2) {

            int i = rng.nextInt(planifiees.size());
            int j = rng.nextInt(planifiees.size());

            while (j == i) {
                j = rng.nextInt(planifiees.size());
            }

            TimeSlot tmp =
                    planifiees.get(i).getSlot();

            planifiees.get(i)
                    .setSlot(planifiees.get(j).getSlot());

            planifiees.get(j)
                    .setSlot(tmp);
        }

        //new creneau slot
        else if (type == 1) {

            PlannedSoutenance s =
                    planifiees.get(
                            rng.nextInt(planifiees.size()));

            s.setSlot(
                    slots.get(
                            rng.nextInt(slots.size())));
        }


        // new salle

        else {

            PlannedSoutenance s =
                    planifiees.get(
                            rng.nextInt(planifiees.size()));

            // IMPORTANT :
            // On cherche une salle libre
            Collections.shuffle(salles, rng);

            for (Salle salle : salles) {

                Salle ancienne = s.getSalle();

                s.setSalle(salle);

                List<PlannedSoutenance> tmp =
                        deepCopy(soutenances);

                if (hardConstraintsOk(tmp, context)) {
                    return;
                }

                // salle invalide → restaurer
                s.setSalle(ancienne);
            }
        }
    }



    private List<PlannedSoutenance> deepCopy(
            List<PlannedSoutenance> original) {

        List<PlannedSoutenance> copy =
                new ArrayList<>(original.size());

        for (PlannedSoutenance s : original) {

            copy.add(
                    PlannedSoutenance.builder()
                            .pfe(s.getPfe())
                            .jury(s.getJury())
                            .salle(s.getSalle())
                            .slot(s.getSlot())
                            .nonPlanifiee(s.isNonPlanifiee())
                            .raisonNonPlacement(
                                    s.getRaisonNonPlacement())
                            .justification(s.getJustification())
                            .build());
        }

        return copy;
    }
}