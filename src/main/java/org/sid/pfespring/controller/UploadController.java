package org.sid.pfespring.controller;



import org.sid.pfespring.dto.ResponseUploadDTO;
import org.sid.pfespring.services.UploadService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

/**
 * Gère l'upload unique du fichier Excel.
 * Le fichier contient TOUTES les feuilles :
 *   - etu               → étudiants
 *   - profs             → professeurs
 *   - pfe_v2            → sujets PFE + CNEs + langue
 *   - salles            → salles de soutenance
 *   - jours_soutenances → date de début (1 seule date, les suivantes calculées auto)
 */
@Controller
@RequestMapping("/home")
public class UploadController {

    private UploadService service;

    public UploadController(UploadService service) {
        this.service = service;
    }


    


    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,HttpSession session) {
        ResponseUploadDTO dto = this.service.importSheets(file); 
        for(int i=2;i<5;i++) session.setAttribute("etape"+i,null);
        session.setAttribute("etape1", true);  
        session.setAttribute("versionId",dto.versionId());
        session.setAttribute("dateDebut", dto.dateDebut());  
        return "upload";
    }

    @GetMapping
    public String welcomePage() {
        return "upload";
    }

    /**
     * Import unique : lit toutes les feuilles du même fichier Excel
     * et stocke les PFEs + la date de début en session.
     */
    // @PostMapping("/upload")
    // public String uploadFile(@RequestParam("file") MultipartFile file,
    //                          HttpSession session) throws Exception {
    //     byte[] bytes = file.getBytes(); // lire une fois, réutiliser plusieurs fois
    //     session.setAttribute("etape1", true);
    //     // 1. Importer étudiants (feuille "etu")
    //     etudiantService.importFromExcel(toMultipartFile(bytes, file.getOriginalFilename()));

    //     // 2. Importer profs (feuille "profs")
    //     profService.importFromExcel(toMultipartFile(bytes, file.getOriginalFilename()));

    //     // 3. Lire les PFEs (feuille "pfe_v2") → mis en session
    //     List<RequestPFEDTO> pfes = pfeService.readExcel(
    //             toMultipartFile(bytes, file.getOriginalFilename()));
    //     session.setAttribute("pfes", pfes);

    //     // 4. Importer les salles (feuille "salles") → en BDD
    //     salleService.importFromExcel(
    //             new java.io.ByteArrayInputStream(bytes));

    //     // 5. Lire la date de début (feuille "jours_soutenances") → en session
    //     java.time.LocalDate dateDebut = salleService.importDateDebut(
    //             new java.io.ByteArrayInputStream(bytes));
    //     session.setAttribute("dateDebut", dateDebut);

    //     return "upload2";
    // }

    // /** Crée un MultipartFile en mémoire à partir des bytes bruts. */
    // private MultipartFile toMultipartFile(byte[] bytes, String filename) {
    //     return new org.springframework.mock.web.MockMultipartFile(
    //             "file", filename,
    //             "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    //             bytes
    //     );
    // }
}