package org.sid.pfespring.controller;

import org.sid.pfespring.config.scheduling.SchedulingConfig;
import org.sid.pfespring.dto.scheduling.SchedulingFormDTO;
import org.sid.pfespring.dto.scheduling.SchedulingResultDTO;
import org.sid.pfespring.model.scheduling.SchedulingSolution;
import org.sid.pfespring.services.scheduling.SchedulingExportService;
import org.sid.pfespring.services.scheduling.SchedulingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Year;

@Controller
@RequestMapping("/scheduling")
public class SchedulingController {

    private final SchedulingService       schedulingService;
    private final SchedulingExportService exportService;

    public SchedulingController(SchedulingService schedulingService,
                                SchedulingExportService exportService) {
        this.schedulingService = schedulingService;
        this.exportService     = exportService;
    }

    // ─────────────────────────────────────────────────────────────────
    //  POST /scheduling/launch — calcul du planning
    //  Reçoit le formulaire inline depuis upload.html
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/launch")
    public String lancer(
            @ModelAttribute SchedulingFormDTO form,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {

        // Vérifier que les jurys sont prêts (flag interne etape3)
        if (session.getAttribute("etape3") == null) {
            ra.addFlashAttribute("errorMessage",
                    "Les jurys doivent être affectés avant de planifier. "
                            + "Réimportez le fichier Excel.");
            return "redirect:/home";
        }

        Long versionId = (Long) session.getAttribute("versionId");
        if (versionId == null) {
            ra.addFlashAttribute("errorMessage",
                    "Aucune version active. Importez d'abord un fichier Excel.");
            return "redirect:/home";
        }

        // Si stratégie non fournie, forcer LES_DEUX
        if (form.getStrategie() == null) {
            form.setStrategie(SchedulingConfig.StrategyMode.LES_DEUX);
        }

        SchedulingResultDTO result = schedulingService.planifier(form, versionId);
        session.setAttribute("lastSchedulingResult", result);

        model.addAttribute("result",    result);
        model.addAttribute("form",      form);
        model.addAttribute("versionId", versionId);

        // Flags pour la nav de result.html
        model.addAttribute("importOk",        Boolean.TRUE.equals(session.getAttribute("importOk")));
        model.addAttribute("etape4Ok",         Boolean.TRUE.equals(session.getAttribute("etape4")));
        model.addAttribute("planningComplet",  Boolean.TRUE.equals(session.getAttribute("planningComplet")));

        return "scheduling/result";
    }

    // ─────────────────────────────────────────────────────────────────
    //  POST /scheduling/valider — persistance + retour /home
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/valider")
    public String valider(
            @RequestParam("algorithme") String algorithme,
            HttpSession session,
            RedirectAttributes ra) {

        Long versionId = (Long) session.getAttribute("versionId");
        SchedulingResultDTO result =
                (SchedulingResultDTO) session.getAttribute("lastSchedulingResult");

        if (result == null || versionId == null) {
            ra.addFlashAttribute("errorMessage",
                    "Résultat introuvable. Relancez le calcul.");
            return "redirect:/home";
        }

        SchedulingSolution solution = switch (algorithme) {
            case "OPTIMISE" -> result.getOptimise();
            default         -> result.getStrict() != null
                    ? result.getStrict()
                    : result.getOptimise();
        };

        if (solution == null) {
            ra.addFlashAttribute("errorMessage",
                    "La solution '" + algorithme + "' n'est pas disponible.");
            return "redirect:/home";
        }

        schedulingService.persisterSolution(solution, versionId);

        boolean complet = solution.getStatus() == SchedulingSolution.Status.COMPLETE;

        // Mise à jour session
        session.setAttribute("etape4",            true);
        session.setAttribute("planningComplet",   complet);
        session.setAttribute("validatedSolution", solution);
        // Invalider PV si on replanifie
        session.removeAttribute("pvGeneres");
        // Garder lastSchedulingResult pour éventuels re-exports depuis result.html
        // mais le supprimer ici pour forcer un recalcul propre
        session.removeAttribute("lastSchedulingResult");

        if (complet) {
            ra.addFlashAttribute("successMessage",
                    "Planning validé — "
                            + solution.getSoutenancesPlanifiees().size()
                            + " soutenance(s) planifiée(s). Téléchargement disponible.");
        } else {
            ra.addFlashAttribute("warnMessage",
                    "Planning partiel validé — "
                            + solution.getSoutenancesPlanifiees().size()
                            + " planifiée(s), "
                            + solution.getSoutenancesEnConflit().size()
                            + " en conflit. "
                            + "Le téléchargement est bloqué jusqu'à obtention d'un planning complet.");
        }

        return "redirect:/home";
    }

    // ─────────────────────────────────────────────────────────────────
    //  GET /scheduling/download/{format}
    //  Téléchargement du planning validé (uniquement si complet)
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/download/{format}")
    public ResponseEntity<byte[]> download(
            @PathVariable String format,
            HttpSession session) throws IOException {

        Boolean complet = (Boolean) session.getAttribute("planningComplet");
        if (!Boolean.TRUE.equals(complet)) {
            throw new RuntimeException(
                    "Téléchargement réservé aux plannings complets sans conflit.");
        }

        SchedulingSolution solution =
                (SchedulingSolution) session.getAttribute("validatedSolution");
        if (solution == null) {
            throw new RuntimeException(
                    "Aucun planning validé en session. Relancez la planification.");
        }

        int    annee = Year.now().getValue();
        byte[] data;
        String ext;

        if ("pdf".equalsIgnoreCase(format)) {
            data = exportService.exportPDF(solution);
            ext  = ".pdf";
        } else {
            data = exportService.exportExcel(solution);
            ext  = ".xlsx";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=planning_soutenances_" + annee + ext)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}