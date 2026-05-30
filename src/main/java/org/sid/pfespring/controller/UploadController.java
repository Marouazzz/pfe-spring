package org.sid.pfespring.controller;

import java.io.IOException;
import java.time.LocalDate;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sid.pfespring.dto.ResponseUploadDTO;
import org.sid.pfespring.services.ImportVersionService;
import org.sid.pfespring.services.JuryService;
import org.sid.pfespring.services.PFEService;
import org.sid.pfespring.services.SalleService;
import org.sid.pfespring.services.UploadService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/home")
public class UploadController {

    private final UploadService        uploadService;
    private final ImportVersionService versionService;
    private final SalleService         salleService;
    private final PFEService           pfeService;
    private final JuryService          juryService;

    public UploadController(
            UploadService uploadService,
            ImportVersionService versionService,
            SalleService salleService,
            PFEService pfeService,
            JuryService juryService) {
        this.uploadService  = uploadService;
        this.versionService = versionService;
        this.salleService   = salleService;
        this.pfeService     = pfeService;
        this.juryService    = juryService;
    }

    // ─────────────────────────────────────────────────────────────────
    //  GET /home
    // ─────────────────────────────────────────────────────────────────
    @GetMapping
    public String home(HttpSession session, Model model) {

        // ── Flags internes (encadrants/jurys gérés auto, pas exposés) ──
        boolean importOk        = Boolean.TRUE.equals(session.getAttribute("importOk"));
        boolean etape4Ok        = Boolean.TRUE.equals(session.getAttribute("etape4"));
        boolean planningComplet = Boolean.TRUE.equals(session.getAttribute("planningComplet"));
        boolean pvGeneres       = Boolean.TRUE.equals(session.getAttribute("pvGeneres"));
        boolean jeuAutoOk       = Boolean.TRUE.equals(session.getAttribute("jeuAutoOk"));
        boolean jeuAutoPartiel  = Boolean.TRUE.equals(session.getAttribute("jeuAutoPartiel"));

        Long      versionId = (Long)      session.getAttribute("versionId");
        LocalDate dateDebut = (LocalDate) session.getAttribute("dateDebut");

        model.addAttribute("importOk",        importOk);
        model.addAttribute("etape4Ok",        etape4Ok);
        model.addAttribute("planningComplet", planningComplet);
        model.addAttribute("pvGeneres",       pvGeneres);
        model.addAttribute("jeuAutoOk",       jeuAutoOk);
        model.addAttribute("jeuAutoPartiel",  jeuAutoPartiel);
        model.addAttribute("versionId",       versionId);
        model.addAttribute("dateDebut",       dateDebut);

        return "upload";
    }

    // ─────────────────────────────────────────────────────────────────
    //  POST /home/upload
    //  Import + affectation encadrants + jurys en cascade automatique
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpSession session,
            RedirectAttributes ra) throws IOException {

        if (file.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Veuillez sélectionner un fichier Excel.");
            return "redirect:/home";
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            ra.addFlashAttribute("errorMessage", "Format invalide. Importez un fichier .xlsx");
            return "redirect:/home";
        }

        try {
            // 1. Import Excel
            ResponseUploadDTO dto = uploadService.importSheets(file);

            XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
            LocalDate dateDebut   = salleService.importDateDebut(
                    workbook.getSheet("jours_soutenances"));
            workbook.close();

            resetWorkflow(session);

            Long finalVersionId = (dto != null && dto.versionId() != null)
                    ? dto.versionId() : versionService.addVersion().getId();

            session.setAttribute("versionId", finalVersionId);
            session.setAttribute("dateDebut",  dateDebut);
            session.setAttribute("importOk",   true);

            // 2. Affectation encadrants automatique
            boolean encOk   = false;
            boolean juryOk  = false;
            StringBuilder warns = new StringBuilder();

            try {
                pfeService.appliquerAffectation(finalVersionId);
                encOk = true;
            } catch (Exception e) {
                warns.append("Encadrants : ").append(e.getMessage()).append(" | ");
            }

            // 3. Affectation jurys automatique (seulement si encadrants OK)
            if (encOk) {
                try {
                    juryService.affecterJury(finalVersionId);
                    juryOk = true;
                } catch (Exception e) {
                    warns.append("Jurys : ").append(e.getMessage());
                }
            }

            // 4. Flags résultat auto-affectation
            if (encOk && juryOk) {
                session.setAttribute("jeuAutoOk", true);
                // etape3 interne : jurys prêts → planification débloquée
                session.setAttribute("etape3", true);
                ra.addFlashAttribute("successMessage",
                        "Fichier importé — encadrants et jurys affectés automatiquement. "
                                + "Version #" + finalVersionId);
            } else {
                session.setAttribute("jeuAutoPartiel", true);
                ra.addFlashAttribute("warnMessage",
                        "Import réussi mais affectation incomplète. "
                                + (warns.length() > 0 ? warns.toString() : "")
                                + " Relancez l'import si nécessaire.");
            }

        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage",
                    "Erreur lors de l'import : " + e.getMessage());
        }

        return "redirect:/home";
    }

    // ─────────────────────────────────────────────────────────────────
    //  Reset complet session (nouvel import)
    // ─────────────────────────────────────────────────────────────────
    private void resetWorkflow(HttpSession session) {
        session.removeAttribute("importOk");
        session.removeAttribute("etape3");
        session.removeAttribute("etape4");
        session.removeAttribute("planningComplet");
        session.removeAttribute("pvGeneres");
        session.removeAttribute("jeuAutoOk");
        session.removeAttribute("jeuAutoPartiel");
        session.removeAttribute("validatedSolution");
        session.removeAttribute("lastSchedulingResult");
        session.removeAttribute("versionId");
        session.removeAttribute("dateDebut");
    }
}