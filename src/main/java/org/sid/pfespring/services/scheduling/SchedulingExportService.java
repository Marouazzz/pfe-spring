package org.sid.pfespring.services.scheduling;

import jakarta.servlet.http.HttpServletResponse;
import org.sid.pfespring.model.scheduling.SchedulingSolution;

import java.io.IOException;

/**
 * Génère les fichiers d'export (Excel, PDF) pour une solution calculée.
 */
public interface SchedulingExportService {

    /** Export Excel du planning. */
    byte[] exportExcel(SchedulingSolution solution) throws IOException;

    /** Export PDF du planning. */
    byte[] exportPDF(SchedulingSolution solution) throws IOException;

}
