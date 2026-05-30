package org.sid.pfespring.model.scheduling;

import lombok.Builder;
import lombok.Getter;

/**
 * Enregistre une violation de contrainte détectée pendant le calcul.
 * Utilisé à la fois pour les contraintes hard (bloquantes)
 * et soft (pénalisantes).
 */
@Getter
@Builder
public class ConstraintViolation {

    public enum Niveau { HARD, SOFT }

    private final String constraintId;
    private final Niveau niveau;
    private final String description;   // "Prof xyz déjà occupé à 10h00 le 15/06" in case chevauchemeant
    private final PlannedSoutenance soutenanceConcernee;
}