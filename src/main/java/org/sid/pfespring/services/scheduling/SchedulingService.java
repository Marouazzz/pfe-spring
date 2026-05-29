package org.sid.pfespring.services.scheduling;

import org.sid.pfespring.dto.scheduling.SchedulingFormDTO;
import org.sid.pfespring.dto.scheduling.SchedulingResultDTO;
import org.sid.pfespring.model.scheduling.SchedulingSolution;

/**
 * Point d'entrée unique du sous-système de planification.
 *
 * Orchestre la construction du contexte, le choix de l'algorithme
 * et la production du SchedulingResultDTO envoyé à la vue.
 */
public interface SchedulingService {

    /**
     * Lance la planification à partir du formulaire soumis.
     *
     * @param form    données saisies par l'utilisateur
     * @param versionId id de la version de données active (stockée en session)
     * @return résultat complet (strict + optimisé + rapport)
     */
    SchedulingResultDTO planifier(SchedulingFormDTO form, Long versionId);

    /**
     * Persiste la solution choisie (strict ou optimisé) dans la table soutenances.
     *
     * @param solution solution validée par l'utilisateur
     * @param versionId version de données concernée
     */
    void persisterSolution(SchedulingSolution solution, Long versionId);
}
