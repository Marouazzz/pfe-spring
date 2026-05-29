package org.sid.pfespring.dto.scheduling;

import lombok.Getter;
import lombok.Setter;
import org.sid.pfespring.config.scheduling.SchedulingConfig;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO soumis par le formulaire de la page de planification.
 * Seuls dateDebut et nombreJours sont obligatoires.
 * Tous les autres champs sont optionnels : si null, le YAML s'applique.
 */
@Getter
@Setter
public class SchedulingFormDTO {

    /** Obligatoire — date de début des soutenances. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateDebut;

    /** Obligatoire — nombre de jours ouvrables souhaités. */
    private int nombreJours;

    /** Optionnel — heure de début de journée (ex: 08:00). */
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime heureDebutJournee;

    /** Optionnel — heure de fin de journée (ex: 18:00). */
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime heureFinJournee;

    /** Optionnel — true si pause déjeuner activée. */
    private Boolean pauseDejeunerActive;

    /**
     * Optionnel — pause minimale (en minutes) entre deux jurys
     * pour un même professeur.
     */
    private Integer pauseEntreJurysMinutes;

    /** Optionnel — max jurys par prof par jour. */
    private Integer maxJurysParProfParJour;

    /** Obligatoire — stratégie choisie par l'utilisateur. */
    private SchedulingConfig.StrategyMode strategie;

    /** ID de la version des données à planifier (stocké en session). */
    private Long versionId;
}