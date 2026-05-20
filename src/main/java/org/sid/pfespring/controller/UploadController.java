package org.sid.pfespring.controller;


import java.io.IOException;
import java.time.LocalDate;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sid.pfespring.dto.ResponseUploadDTO;
import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.services.ImportVersionService;
import org.sid.pfespring.services.SalleService;
import org.sid.pfespring.services.UploadService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/home")
public class UploadController {

    private final UploadService uploadService;
    private final ImportVersionService versionService;
    private final SalleService salleService;

    public UploadController(
            UploadService uploadService,
            ImportVersionService versionService,
            SalleService salleService
    ) {
        this.uploadService = uploadService;
        this.versionService = versionService;
        this.salleService = salleService;
    }

    /**
     * =========================
     * PAGE PRINCIPALE
     * =========================
     */
    @GetMapping
    public String home(HttpSession session, Model model) {

        // =========================
        // FLAGS ETAPES
        // =========================
        boolean etape1Ok = session.getAttribute("etape1") != null;
        boolean etape2Ok = session.getAttribute("etape2") != null;
        boolean etape3Ok = session.getAttribute("etape3") != null;
        boolean etape4Ok = session.getAttribute("etape4") != null;
        boolean etape5Ok = session.getAttribute("etape5") != null;

        // =========================
        // FLAGS EXPORTS
        // =========================
        boolean exportEncadrantsOk =
                session.getAttribute("exportEncadrantsOk") != null;

        boolean exportJurysOk =
                session.getAttribute("exportJurysOk") != null;

        boolean exportSoutenancesOk =
                session.getAttribute("exportSoutenancesOk") != null;

        // =========================
        // DATA SESSION
        // =========================
        Long versionId = (Long) session.getAttribute("versionId");

        LocalDate dateDebut =
                (LocalDate) session.getAttribute("dateDebut");

        // =========================
        // MODEL
        // =========================
        model.addAttribute("etape1Ok", etape1Ok);
        model.addAttribute("etape2Ok", etape2Ok);
        model.addAttribute("etape3Ok", etape3Ok);
        model.addAttribute("etape4Ok", etape4Ok);
        model.addAttribute("etape5Ok", etape5Ok);

        model.addAttribute("exportEncadrantsOk", exportEncadrantsOk);
        model.addAttribute("exportJurysOk", exportJurysOk);
        model.addAttribute("exportSoutenancesOk", exportSoutenancesOk);

        model.addAttribute("versionId", versionId);
        model.addAttribute("dateDebut", dateDebut);

        return "upload";
    }


    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpSession session
    ) throws IOException {
            if (file.isEmpty()) throw new RuntimeException("Veuillez sélectionner un fichier Excel.");

            String filename = file.getOriginalFilename();
            if(filename.isEmpty() || !filename.endsWith(".xlsx")) throw new IllegalArgumentException("Essayer d'importer un fichier de type .xlsx");

            // =========================
            // NOUVELLE VERSION
            // =========================
            ImportVersion version = versionService.addVersion();

            // =========================
            // ANCIENNE LOGIQUE IMPORT
            // =========================
            ResponseUploadDTO dto =
                    uploadService.importSheets(file);


            XSSFWorkbook workbook =
                    new XSSFWorkbook(file.getInputStream());

            LocalDate dateDebut =
                    salleService.importDateDebut(
                            workbook.getSheet("jours_soutenances")
                    );

            workbook.close();

            resetWorkflow(session);


            session.setAttribute("etape1", true);

            // priorité à dto sinon version
            if (dto != null && dto.versionId() != null) {
                session.setAttribute(
                        "versionId",
                        dto.versionId()
                );
            } else {
                session.setAttribute(
                        "versionId",
                        version.getId()
                );
            }

            // priorité dto sinon excel
            if (dto != null && dto.dateDebut() != null) {
                session.setAttribute(
                        "dateDebut",
                        dto.dateDebut()
                );
            } else {
                session.setAttribute(
                        "dateDebut",
                        dateDebut
                );
            }

            session.setAttribute(
                    "uploadSuccess",
                    "Fichier importé avec succès."
            );
        return "redirect:/home";
    }


    private void resetWorkflow(HttpSession session) {

        // étapes
        session.removeAttribute("etape1");
        session.removeAttribute("etape2");
        session.removeAttribute("etape3");
        session.removeAttribute("etape4");
        session.removeAttribute("etape5");

        // exports
        session.removeAttribute("exportEncadrantsOk");
        session.removeAttribute("exportJurysOk");
        session.removeAttribute("exportSoutenancesOk");

        // messages
        session.removeAttribute("uploadError");
        session.removeAttribute("uploadSuccess");
    }
}