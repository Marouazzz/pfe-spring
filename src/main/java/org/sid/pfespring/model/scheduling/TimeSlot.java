package org.sid.pfespring.model.scheduling;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Représente un créneau horaire (date + heure début + heure fin).
 * Value object immuable utilisé comme "slot" candidat pour une soutenance.
 */
@Getter
@Builder
public class TimeSlot {

    private final LocalDate date;
    private final LocalTime heureDebut;
    private final LocalTime heureFin;

    /**
     * Vérifie si ce slot chevauche un autre slot.
     * Utilisé par les contraintes de non-conflit.
     */
    public boolean chevauchePare(TimeSlot autre) {
        if (!this.date.equals(autre.date)) return false;
        return this.heureDebut.isBefore(autre.heureFin)
                && autre.heureDebut.isBefore(this.heureFin);
    }

    /**
     * Calcule la durée en minutes séparant la FIN de ce slot
     * et le DÉBUT du slot "autre" (dans la même journée).
     * Retourne une valeur négative si les slots se chevauchent.
     */
    public long minutesJusquA(TimeSlot autre) {
        if (!this.date.equals(autre.date)) return Long.MAX_VALUE;
        return java.time.Duration.between(this.heureFin, autre.heureDebut).toMinutes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot t)) return false;
        return Objects.equals(date, t.date)
                && Objects.equals(heureDebut, t.heureDebut)
                && Objects.equals(heureFin, t.heureFin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, heureDebut, heureFin);
    }

    @Override
    public String toString() {
        return date + " " + heureDebut + "-" + heureFin;
    }
}