package org.sid.pfespring.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.sid.pfespring.services.FileSystemService;
import org.sid.pfespring.services.JuryService;
import org.sid.pfespring.services.PFEService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

/**
 * Permet de télécharger tous les PV d'une version sous forme d'un ZIP.
 * Les fichiers sont générés par FileSystemServiceImpl dans ${pv.root}.
 *
 * GET /pv/download?id={versionId} → télécharge PV_v{id}.zip
 */
@Controller
@RequestMapping("/pv")
public class PvController {

    @Value("${pv.root}")
    private String rootFolder;

    private JuryService juryService;
    private PFEService pFEService;
    private FileSystemService fsService;

    public PvController(JuryService juryService,PFEService pfeService,FileSystemService fSystemService){
        this.juryService = juryService;
        this.pFEService = pfeService;
        this.fsService = fSystemService;
    }

    /**
     * Zippe tous les sous-dossiers qui se terminent par "_v{id}"
     * et renvoie le ZIP en téléchargement.

    @GetMapping("/download")
    public Object downloadPVs(@RequestParam("id") Long id, HttpSession session) throws IOException {

        // Sécurité : vérifier que les étapes sont complètes
        if (session.getAttribute("etape1") == null ||
                session.getAttribute("etape2") == null ||
                session.getAttribute("etape3") == null ||
                session.getAttribute("etape4") == null) {
            return "redirect:/erreur?message=Vous devez compléter les étapes précédentes";
        }
        session.setAttribute("etape5", true);
        Path root = Paths.get(rootFolder);
        if (!Files.exists(root)) {
            return ResponseEntity.notFound().build();
        }
        this.pFEService.createPVFolder(id);
        this.juryService.genererPV(id);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // On parcourt les dossiers du type NomProf_PrenomProf_v{id}
            Files.list(root)
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().endsWith("_v" + id))
                    .forEach(profDir -> {
                        try {
                            Files.list(profDir)
                                    .filter(f -> f.toString().endsWith(".docx"))
                                    .forEach(file -> {
                                        try {
                                            String zipEntryName = profDir.getFileName() + "/" + file.getFileName();
                                            zos.putNextEntry(new ZipEntry(zipEntryName));
                                            zos.write(Files.readAllBytes(file));
                                            zos.closeEntry();
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        byte[] zipBytes = baos.toByteArray();
        if (zipBytes.length == 0) {
            return ResponseEntity.noContent().build();
        }
        fsService.deletePVFolder(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=PV_v" + id + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipBytes);
    }
     */
    @PostMapping("/generer")
    public String genererPVs(HttpSession session) throws IOException {
        if (session.getAttribute("etape1") == null ||
                session.getAttribute("etape2") == null ||
                session.getAttribute("etape3") == null ||
                session.getAttribute("etape4") == null) {
            return "redirect:/erreur?message=Vous devez compléter les étapes précédentes";
        }
        Long id = (Long) session.getAttribute("versionId");
        pFEService.createPVFolder(id);
        juryService.genererPV(id);

        session.setAttribute("etape5", true);
        session.setAttribute("pvGeneres", true); // ← flag pour afficher le bouton de téléchargement

        return "redirect:/home";
    }

    /**
     * NOUVEAU — Téléchargement ZIP des PV (sans regénérer).
     * Séparé de la génération pour respecter le même pattern que les autres exports.
     */
    @GetMapping("/download")
    public Object downloadPVs(HttpSession session) throws IOException {
        Long id = (Long) session.getAttribute("versionId");
        if (id == null || session.getAttribute("pvGeneres") == null) {
            return "redirect:/erreur?message=Vous devez d'abord générer les PV";
        }

        Path root = Paths.get(rootFolder);
        if (!Files.exists(root)) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            Files.list(root)
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().endsWith("_v" + id))
                    .forEach(profDir -> {
                        try {
                            Files.list(profDir)
                                    .filter(f -> f.toString().endsWith(".docx"))
                                    .forEach(file -> {
                                        try {
                                            String zipEntryName = profDir.getFileName() + "/" + file.getFileName();
                                            zos.putNextEntry(new ZipEntry(zipEntryName));
                                            zos.write(Files.readAllBytes(file));
                                            zos.closeEntry();
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        byte[] zipBytes = baos.toByteArray();
        if (zipBytes.length == 0) {
            return ResponseEntity.noContent().build();
        }

        fsService.deletePVFolder(id);
        // Réinitialiser le flag pour forcer une nouvelle génération si re-téléchargement
        session.removeAttribute("pvGeneres");
        session.setAttribute("etape5", true);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=PV_v" + id + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipBytes);
    }

}