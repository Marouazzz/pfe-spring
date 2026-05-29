package org.sid.pfespring.config.scheduling;

import org.sid.pfespring.dto.scheduling.SchedulingFormDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Charge les valeurs par défaut depuis scheduling-rules.yml
 * et les fusionne avec les valeurs soumises via le formulaire web.
 *
 * Le formulaire a toujours priorité sur le YAML.
 * Le YAML a toujours priorité sur les valeurs codées ici.
 */
@Component
public class SchedulingConfigLoader {

    // ─── YAML VALEURS ────────────────────────────────────────────────────
    @Value("${scheduling.creneaux.heure-debut-journee:08:00}")
    private String heureDebutJournee;

    @Value("${scheduling.creneaux.heure-fin-journee:18:00}")
    private String heureFinJournee;

    @Value("${scheduling.creneaux.pause-dejeuner.active:true}")
    private boolean pauseDejActive;

    @Value("${scheduling.creneaux.pause-dejeuner.debut:12:00}")
    private String pauseDejDebut;

    @Value("${scheduling.creneaux.pause-dejeuner.fin:14:00}")
    private String pauseDejFin;

    @Value("${scheduling.creneaux.pause-entre-jurys-prof-minutes:60}")
    private int pauseEntreProfMinutes;

    @Value("${scheduling.jury.membres-minimum:3}")
    private int juryMembresMinimum;

    @Value("${scheduling.jury.membres-maximum:3}")
    private int juryMembresMaximum;

    @Value("${scheduling.jury.preferer-prof-langue:true}")
    private boolean prefererProfLangue;

    @Value("${scheduling.charge.max-jurys-par-prof-par-jour:4}")
    private int maxJurysParProfParJour;

    @Value("${scheduling.charge.equilibrer-charge:true}")
    private boolean equilibrerCharge;

    @Value("${scheduling.salles.verifier-capacite:true}")
    private boolean verifierCapaciteSalle;

    @Value("${scheduling.algorithme.optimise.iterations-max:2000}")
    private int iterationsMax;

    @Value("${scheduling.algorithme.optimise.temps-limite-secondes:30}")
    private int tempsLimite;

    @Value("${scheduling.algorithme.optimise.temperature-initiale:100.0}")
    private double temperatureInitiale;

    @Value("${scheduling.algorithme.optimise.taux-refroidissement:0.995}")
    private double tauxRefroidissement;

    @Value("${scheduling.conflits.planning-partiel-telechargeable:true}")
    private boolean planningPartielTelechargeable;

    /**
     * Point d'entrée principal.
     * Fusionne les valeurs formulaire + YAML en un seul SchedulingConfig immuable.
     */
    public SchedulingConfig buildFrom(SchedulingFormDTO form) {

        // Priorité formulaire pour la pause entre jurys
        int pauseProf = form.getPauseEntreJurysMinutes() != null
                ? form.getPauseEntreJurysMinutes()
                : pauseEntreProfMinutes;

        // Max jurys/prof/jour : formulaire prioritaire
        int maxJurys = form.getMaxJurysParProfParJour() != null
                ? form.getMaxJurysParProfParJour()
                : maxJurysParProfParJour;

        // Pause déjeuner : désactivée si l'utilisateur ne la veut pas
        boolean pauseDej = form.getPauseDejeunerActive() != null
                ? form.getPauseDejeunerActive()
                : pauseDejActive;

        // Créneaux : formulaire prioritaire
        LocalTime debut = form.getHeureDebutJournee() != null
                ? form.getHeureDebutJournee()
                : LocalTime.parse(heureDebutJournee);

        LocalTime fin = form.getHeureFinJournee() != null
                ? form.getHeureFinJournee()
                : LocalTime.parse(heureFinJournee);

        // Stratégie : formulaire obligatoire
        SchedulingConfig.StrategyMode strategie =
                form.getStrategie() != null
                        ? form.getStrategie()
                        : SchedulingConfig.StrategyMode.LES_DEUX;

        // Objectifs YAML par défaut (non modifiables via formulaire simple)
        List<SchedulingConfig.ObjectifConfig> objectifs = buildDefaultObjectifs();

        return SchedulingConfig.builder()
                .dateDebut(form.getDateDebut())
                .nombreJours(form.getNombreJours())
                .heureDebutJournee(debut)
                .heureFinJournee(fin)
                .dureeSoutenanceMinutes(60)                   // FIXE
                .pauseDejeunerActive(pauseDej)
                .pauseDejeunerDebut(LocalTime.parse(pauseDejDebut))
                .pauseDejeunerFin(LocalTime.parse(pauseDejFin))
                .pauseEntreProfMinutes(pauseProf)
                .juryMembresMinimum(juryMembresMinimum)
                .juryMembresMaximum(juryMembresMaximum)
                .prefererProfLangue(prefererProfLangue)
                .maxJurysParProfParJour(maxJurys)
                .equilibrerCharge(equilibrerCharge)
                .verifierCapaciteSalle(verifierCapaciteSalle)
                .strategie(strategie)
                .optimisationIterationsMax(iterationsMax)
                .optimisationTempsLimiteSecondes(tempsLimite)
                .optimisationTemperatureInitiale(temperatureInitiale)
                .optimisationTauxRefroidissement(tauxRefroidissement)
                .planningPartielTelechargeable(planningPartielTelechargeable)
                .objectifs(objectifs)
                .build();
    }

    private List<SchedulingConfig.ObjectifConfig> buildDefaultObjectifs() {
        return List.of(
                SchedulingConfig.ObjectifConfig.builder()
                        .id("equilibre_charge_jury")
                        .actif(true).poids(1.0)
                        .description("Répartir équitablement la charge entre les profs")
                        .build(),
                SchedulingConfig.ObjectifConfig.builder()
                        .id("minimiser_trous_horaires")
                        .actif(true).poids(0.8)
                        .description("Réduire les créneaux vides dans la journée")
                        .build(),
                SchedulingConfig.ObjectifConfig.builder()
                        .id("preference_langue")
                        .actif(true).poids(0.5)
                        .description("Préférer un prof dont la spécialité correspond à la langue du PFE")
                        .build(),
                SchedulingConfig.ObjectifConfig.builder()
                        .id("regrouper_par_encadrant")
                        .actif(false).poids(0.4)
                        .description("Mettre les soutenances d'un même encadrant consécutives")
                        .build()
        );
    }
}