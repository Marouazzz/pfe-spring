package org.sid.pfespring.config.scheduling;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Objet de configuration unifié.
 * Alimenté par : formulaire web (dateDebut, nombreJours, etc.)
 *                + YAML (créneaux, pause, composition jury, algorithme)
 *
 * Principe OCP : cet objet est lu par le moteur — jamais modifié.
 * Pour ajouter un paramètre : l'ajouter ici + dans scheduling-rules.yml.
 */
@Getter
@Builder
public class SchedulingConfig {

    // ─── FENÊTRE TEMPORELLE (formulaire) ───────────────────────────────────
    private final LocalDate dateDebut;
    private final int nombreJours;

    // ─── CRÉNEAUX (yaml ou formulaire) ─────────────────────────────────────
    private final LocalTime heureDebutJournee;
    private final LocalTime heureFinJournee;

    /** Durée fixe d'une soutenance — TOUJOURS 60 min. */
    private final int dureeSoutenanceMinutes;

    private final boolean pauseDejeunerActive;
    private final LocalTime pauseDejeunerDebut;
    private final LocalTime pauseDejeunerFin;

    /**
     * Pause minimale obligatoire entre deux jurys pour un même prof (en minutes).
     * Le YAML fixe la valeur par défaut (60). Le formulaire peut la surcharger.
     */
    private final int pauseEntreProfMinutes;

    // ─── COMPOSITION JURY (yaml) ────────────────────────────────────────────
    private final int juryMembresMinimum;
    private final int juryMembresMaximum;

    /**
     * La règle "langue" est SOFT uniquement.
     * true  = on cherche un prof dont la spécialité correspond à la langue du PFE.
     * false = on ignore totalement la spécialité.
     */
    private final boolean prefererProfLangue;

    // ─── CHARGE (yaml) ─────────────────────────────────────────────────────
    private final int maxJurysParProfParJour;

    /**
     * Répartition logique : charge/prof ≈ totalSoutenances / nbProfs.
     * Si true, le moteur calcule automatiquement la cible et l'utilise
     * comme contrainte molle.
     */
    private final boolean equilibrerCharge;

    // ─── SALLES (yaml) ──────────────────────────────────────────────────────
    private final boolean verifierCapaciteSalle;

    // ─── ALGORITHME (formulaire) ────────────────────────────────────────────
    private final StrategyMode strategie;

    private final int optimisationIterationsMax;
    private final int optimisationTempsLimiteSecondes;
    private final double optimisationTemperatureInitiale;
    private final double optimisationTauxRefroidissement;

    // ─── OBJECTIFS SOFT (yaml) ─────────────────────────────────────────────
    private final List<ObjectifConfig> objectifs;

    // ─── COMPORTEMENT CONFLIT (yaml) ────────────────────────────────────────
    private final boolean planningPartielTelechargeable;

    // ──────────────────────────────────────────────────────────────────────
    //  ENUM STRATÉGIE
    // ──────────────────────────────────────────────────────────────────────
    public enum StrategyMode {
        STRICT,
        OPTIMISE,
        LES_DEUX
    }

    // ──────────────────────────────────────────────────────────────────────
    //  OBJECTIF SOFT CONFIGURABLE
    // ──────────────────────────────────────────────────────────────────────
    @Getter
    @Builder
    public static class ObjectifConfig {
        private final String id;
        private final boolean actif;
        private final double poids;
        private final String description;
    }
}