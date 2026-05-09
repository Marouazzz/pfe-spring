package org.sid.pfespring.controller;


import java.io.IOException;
import java.time.Year;

import org.sid.pfespring.dto.RequestJuryDTO;
import org.sid.pfespring.dto.ResponseJuryDTO;
import org.sid.pfespring.services.JuryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/jurys")
public class JuryController extends AbstractController<RequestJuryDTO, ResponseJuryDTO> {

    private final JuryService juryService;

    public JuryController(JuryService juryService) {
        super(juryService);
        this.juryService = juryService;
    }

//    @GetMapping("/affectations")
//    public ResponseEntity<byte[]> affecterJury(@RequestParam("id") Long id,HttpSession session) throws IOException {
//        juryService.affecterJury(id);
//        session.setAttribute("etape3",true);
//        byte[] fichier = juryService.exportJuryExcel(id);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=jury_affectations.xlsx")
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(fichier);
//    }
    @GetMapping("/affectations")
    public Object affecterJury(@RequestParam("id") Long id, HttpSession session) throws IOException {
        int annee = Year.now().getValue();
        if (session.getAttribute("etape1") == null ||
                session.getAttribute("etape2") == null) {
            return "redirect:/erreur?message=Vous devez compléter les étapes précédentes avant d'affecter les jurys.";
        }

        juryService.affecterJury(id);
        session.setAttribute("etape3", true);
        session.setAttribute("versionId", id);
        session.removeAttribute("etape4");
        byte[] fichier = juryService.exportJuryExcel(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=jury_affectations_" + annee + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fichier);
    }
}