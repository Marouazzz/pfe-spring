package org.sid.pfespring.services.scheduling;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.sid.pfespring.model.scheduling.PlannedSoutenance;
import org.sid.pfespring.model.scheduling.SchedulingSolution;
import org.sid.pfespring.utils.ExcelGenerator;
import org.sid.pfespring.utils.PDFGenerator;
import org.springframework.stereotype.Service;

/**
 * Génère les exports Excel et PDF d'une {@link SchedulingSolution}.
 *
 * Tri canonique : date ASC → heure début ASC → salle ASC
 * (même ordre que l'affichage à l'écran)
 *
 * Colonnes : Date | Début | Fin | Salle | Filière | Sujet PFE
 *            | Étudiant(s) | Encadrant | Membre 1 | Membre 2
 */
@Service
public class SchedulingExportServiceImpl implements SchedulingExportService {

    // ─── Tri canonique centralisé ─────────────────────────────────────────

    private static List<PlannedSoutenance> sorted(SchedulingSolution solution) {
        return solution.getSoutenancesPlanifiees().stream()
                .sorted(Comparator
                        .comparing((PlannedSoutenance ps) ->
                                ps.getSlot() != null ? ps.getSlot().getDate() : LocalDate.MAX)
                        .thenComparing(ps ->
                                ps.getSlot() != null ? ps.getSlot().getHeureDebut()
                                        : java.time.LocalTime.MAX)
                        .thenComparing(ps ->
                                ps.getSalle() != null ? ps.getSalle().getNomSalle() : ""))
                .toList();
    }
    
    @Override
    public byte[] exportExcel(SchedulingSolution solution) throws IOException {
        List<PlannedSoutenance> planifiees = sorted(solution);
        return ExcelGenerator.exportPlanning(planifiees, solution);
    }
    @Override
    public byte[] exportPDF(SchedulingSolution solution) throws IOException {
        List<PlannedSoutenance> planifiees = sorted(solution);
        return PDFGenerator.exportPlanning(planifiees, solution);
    }
}