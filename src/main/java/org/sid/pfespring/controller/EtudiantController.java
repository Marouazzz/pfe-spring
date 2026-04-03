package org.sid.pfespring.controller;
import org.sid.pfespring.dto.RequestEtudiantDTO;
import org.sid.pfespring.dto.ResponseEtudiantDTO;
import org.sid.pfespring.services.EtudiantService;
import org.sid.pfespring.services.ProfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/etudiants")
public class EtudiantController extends AbstractController<
        RequestEtudiantDTO,
        ResponseEtudiantDTO>
{
private final EtudiantService etudiantService;

    public EtudiantController(EtudiantService service) {
        super(service);
        this.etudiantService = service;
    }

    @PostMapping("/import")
    public ResponseEntity<List<ResponseEtudiantDTO>> importExcel(
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(etudiantService.importFromExcel(file));
    }
}