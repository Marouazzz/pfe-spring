package org.sid.pfespring.services.scheduling;

import org.sid.pfespring.config.scheduling.SchedulingConfig;
import org.sid.pfespring.model.scheduling.TimeSlot;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Génère la liste ordonnée des créneaux horaires disponibles
 * à partir de la configuration de planification.
 *
 * Règles appliquées :
 * - Pas de week-ends (FIXE)
 * - Plage horaire configurable (heureDebut / heureFin)
 * - Durée fixe de 60 minutes
 * - Pause déjeuner si activée
 */
@Component
public class SlotGenerator {

    /**
     * Génère tous les créneaux disponibles sur la fenêtre de planification.
     *
     * @param config configuration fusionnée
     * @return liste de TimeSlots ordonnés par date puis heure
     */
    public List<TimeSlot> generate(SchedulingConfig config) {
        List<TimeSlot> slots = new ArrayList<>();

        LocalDate date = config.getDateDebut();
        int joursAjoutes = 0;

        // Avancer jusqu'au nombre de jours ouvrables demandés
        while (joursAjoutes < config.getNombreJours()) {

            // Ignorer les week-ends (FIXE)
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY
                    || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                date = date.plusDays(1);
                continue;
            }

            slots.addAll(generateForDay(date, config));
            joursAjoutes++;
            date = date.plusDays(1);
        }

        return slots;
    }

    private List<TimeSlot> generateForDay(LocalDate date, SchedulingConfig config) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime cursor = config.getHeureDebutJournee();
        LocalTime finJournee = config.getHeureFinJournee();
        int duree = config.getDureeSoutenanceMinutes();

        while (!cursor.plusMinutes(duree).isAfter(finJournee)) {
            LocalTime fin = cursor.plusMinutes(duree);

            // Vérifier intersection avec pause déjeuner
            if (config.isPauseDejeunerActive()) {
                LocalTime debutPause = config.getPauseDejeunerDebut();
                LocalTime finPause   = config.getPauseDejeunerFin();

                boolean intersectePause = cursor.isBefore(finPause)
                        && fin.isAfter(debutPause);

                if (intersectePause) {
                    // Sauter à la fin de la pause
                    cursor = finPause;
                    continue;
                }
            }

            slots.add(TimeSlot.builder()
                    .date(date)
                    .heureDebut(cursor)
                    .heureFin(fin)
                    .build());

            cursor = fin;
        }

        return slots;
    }
}