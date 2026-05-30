package org.sid.pfespring.controller;

import org.sid.pfespring.dto.scheduling.SchedulingResultDTO;
import org.sid.pfespring.model.scheduling.SchedulingSolution;
import org.sid.pfespring.services.scheduling.SchedulingExportService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/scheduling/export")
public class SchedulingExportController {

    private final SchedulingExportService exportService;

    public SchedulingExportController(SchedulingExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/{type}/{format}")
    public void exporter(
            @PathVariable String type,
            @PathVariable String format,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        // Récupérer la solution depuis la session
        SchedulingSolution solution = (SchedulingSolution) session.getAttribute("selectedSolution");

        if (solution == null) {
            // Si pas de solution validée, prendre depuis le dernier résultat
            var result = (SchedulingResultDTO) session.getAttribute("lastSchedulingResult");
            if (result != null) {
                solution = "strict".equalsIgnoreCase(type) ? result.getStrict() : result.getOptimise();
            }
        }

        if (solution == null) {
            response.sendError(400, "Aucune solution disponible. Veuillez d'abord planifier.");
            return;
        }

        if ("excel".equalsIgnoreCase(format)) {
            byte[] excelData = exportService.exportExcel(solution);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=planning_" + type + ".xlsx");
            response.getOutputStream().write(excelData);

        } else if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdfData = exportService.exportPDF(solution);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=planning_" + type + ".pdf");
            response.getOutputStream().write(pdfData);
        }
    }
}