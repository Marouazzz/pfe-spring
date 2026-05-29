package org.sid.pfespring.services.scheduling;

import org.sid.pfespring.config.scheduling.SchedulingConfig;
import org.sid.pfespring.config.scheduling.SchedulingConfigLoader;
import org.sid.pfespring.dto.scheduling.SchedulingFormDTO;
import org.sid.pfespring.dto.scheduling.SchedulingResultDTO;
import org.sid.pfespring.engine.JustificationEngine;
import org.sid.pfespring.engine.OptimisedSchedulingEngine;
import org.sid.pfespring.engine.ScoreCalculator;
import org.sid.pfespring.engine.StrictSchedulingEngine;
import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.model.Jury;
import org.sid.pfespring.model.Salle;
import org.sid.pfespring.model.Soutenance;
import org.sid.pfespring.model.scheduling.*;
import org.sid.pfespring.repository.*;
import org.sid.pfespring.services.scheduling.SlotGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implémentation principale du {@link SchedulingService}.
 *
 * Pipeline :
 * 1. Charger config (formulaire + YAML)
 * 2. Charger les données depuis la BDD (jurys, salles)
 * 3. Générer les slots disponibles
 * 4. Construire le SchedulingContext
 * 5. Lancer l'algorithme selon la stratégie choisie
 * 6. Calculer les scores
 * 7. Produire le rapport de comparaison si LES_DEUX
 * 8. Retourner le SchedulingResultDTO
 */
@Service
public class SchedulingServiceImpl implements SchedulingService {

    private final SchedulingConfigLoader  configLoader;
    private final SlotGenerator slotGenerator;
    private final StrictSchedulingEngine  strictEngine;
    private final OptimisedSchedulingEngine optimisedEngine;
    private final ScoreCalculator         scoreCalculator;
    private final JustificationEngine     justificationEngine;

    private final ImportVersionRepository versionRepo;
    private final JuryRepository          juryRepo;
    private final SalleRepository         salleRepo;
    private final SoutenanceRepository    soutenanceRepo;

    public SchedulingServiceImpl(
            SchedulingConfigLoader configLoader,
            SlotGenerator slotGenerator,
            StrictSchedulingEngine strictEngine,
            OptimisedSchedulingEngine optimisedEngine,
            ScoreCalculator scoreCalculator,
            JustificationEngine justificationEngine,
            ImportVersionRepository versionRepo,
            JuryRepository juryRepo,
            SalleRepository salleRepo,
            SoutenanceRepository soutenanceRepo) {

        this.configLoader       = configLoader;
        this.slotGenerator      = slotGenerator;
        this.strictEngine       = strictEngine;
        this.optimisedEngine    = optimisedEngine;
        this.scoreCalculator    = scoreCalculator;
        this.justificationEngine = justificationEngine;
        this.versionRepo        = versionRepo;
        this.juryRepo           = juryRepo;
        this.salleRepo          = salleRepo;
        this.soutenanceRepo     = soutenanceRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public SchedulingResultDTO planifier(SchedulingFormDTO form, Long versionId) {

        //Config
        form.setVersionId(versionId);
        SchedulingConfig config = configLoader.buildFrom(form);

        // data
        ImportVersion version = versionRepo.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version introuvable : " + versionId));

        List<Jury>     jurys  = juryRepo.findAllWithRelations(version);
        List<Salle>    salles = salleRepo.findByVersionAndDisponibleTrue(version);

        if (jurys.isEmpty())
            throw new RuntimeException("Aucun jury trouvé pour cette version. "
                    + "Veuillez d'abord affecter les jurys (Étape 3).");

        if (salles.isEmpty())
            throw new RuntimeException("Aucune salle disponible pour cette version. "
                    + "Vérifiez l'import Excel.");

        // slots
        List<TimeSlot> slots = slotGenerator.generate(config);
        if (slots.isEmpty())
            throw new RuntimeException("Aucun créneau généré avec ces paramètres. "
                    + "Vérifiez les dates et horaires.");

        // context de plani
        SchedulingContext context = SchedulingContext.builder()
                .jurys(jurys)
                .salles(salles)
                .config(config)
                .slotsDisponibles(slots)
                .build();

        //algos et scores
        SchedulingConfig.StrategyMode strategie = config.getStrategie();

        SchedulingSolution strict   = null;
        SchedulingSolution optimise = null;

        if (strategie == SchedulingConfig.StrategyMode.STRICT
                || strategie == SchedulingConfig.StrategyMode.LES_DEUX) {
            strict = strictEngine.planifier(context);
            SolutionScore score = scoreCalculator.calculer(strict);
            strict = rebuildWithScore(strict, score);
        }

        if (strategie == SchedulingConfig.StrategyMode.OPTIMISE
                || strategie == SchedulingConfig.StrategyMode.LES_DEUX) {
            optimise = optimisedEngine.planifier(context);
            SolutionScore score = scoreCalculator.calculer(optimise);
            optimise = rebuildWithScore(optimise, score);
        }

        // compa
        ComparisonReport rapport = null;
        if (strict != null && optimise != null) {
            rapport = justificationEngine.comparer(strict, optimise);
        }

        //result dto
        boolean hasConflicts = (strict != null
                && strict.getStatus() == SchedulingSolution.Status.PARTIELLE)
                || (optimise != null
                && optimise.getStatus() == SchedulingSolution.Status.PARTIELLE);

        String alerte = strict != null ? strict.getAlerteMessage() : null;

        return SchedulingResultDTO.builder()
                .strict(strict)
                .optimise(optimise)
                .rapport(rapport)
                .hasConflicts(hasConflicts)
                .alerteMessage(alerte)
                .build();
    }

    @Override
    @Transactional
    public void persisterSolution(SchedulingSolution solution, Long versionId) {

        ImportVersion version = versionRepo.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version introuvable"));

        // Supprimer les anciennes soutenances de cette version
        soutenanceRepo.deleteSoutenancesByVersion(version);

        // Persister uniquement les soutenances planifiées
        List<Soutenance> entites = solution.getSoutenancesPlanifiees().stream()
                .map(ps -> Soutenance.builder()
                        .pfe(ps.getPfe())
                        .jury(ps.getJury())
                        .salle(ps.getSalle())
                        .dateSoutenance(ps.getSlot().getDate())
                        .heureDebut(ps.getSlot().getHeureDebut())
                        .heureFin(ps.getSlot().getHeureFin())
                        .version(version)
                        .build())
                .toList();

        soutenanceRepo.saveAll(entites);
    }

    // helper pour reconstruire la solution immuable avec le score calculé

    private SchedulingSolution rebuildWithScore(SchedulingSolution sol, SolutionScore score) {
        return SchedulingSolution.builder()
                .status(sol.getStatus())
                .soutenances(sol.getSoutenances())
                .violationsSoft(sol.getViolationsSoft())
                .score(score)
                .algorithme(sol.getAlgorithme())
                .dureeCalculMs(sol.getDureeCalculMs())
                .justifications(sol.getJustifications())
                .build();
    }
}
