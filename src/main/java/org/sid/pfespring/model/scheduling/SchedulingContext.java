package org.sid.pfespring.model.scheduling;

import lombok.Builder;
import lombok.Getter;
import org.sid.pfespring.config.scheduling.SchedulingConfig;
import org.sid.pfespring.model.Jury;
import org.sid.pfespring.model.PFE;
import org.sid.pfespring.model.Salle;

import java.util.List;

/**
 * Contexte complet passé aux algorithmes de planification.
 * Immuable — le moteur lit mais ne modifie pas le contexte.
 *
 * Contient toutes les données nécessaires au calcul :
 * données métier + configuration.
 */
@Getter
@Builder
public class SchedulingContext {

    /** PFEs ayant un jury affecté — prêts à être planifiés. */
    private final List<PFE> pfes;

    /** Jurys déjà constitués (résultat de l'étape jury). */
    private final List<Jury> jurys;

    /** Salles disponibles pour la version courante. */
    private final List<Salle> salles;

    /** Configuration fusionnée (formulaire + YAML). */
    private final SchedulingConfig config;

    /** Slots générés à partir de la config (date début + nb jours + créneaux). */
    private final List<TimeSlot> slotsDisponibles;
}