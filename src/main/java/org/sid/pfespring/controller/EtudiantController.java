package org.sid.pfespring.controller;

import lombok.RequiredArgsConstructor;
import org.sid.pfespring.dto.EtudiantDTO;
import org.sid.pfespring.services.EtudiantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/etudiants")

public class EtudiantController {

    private final EtudiantService etudiantService;
    public EtudiantController(EtudiantService etudiantService) {
        this.etudiantService = etudiantService;
    }

    @PostMapping("/import")
    public ResponseEntity<List<EtudiantDTO.Response>> importerExcel(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<EtudiantDTO.Response> imported = etudiantService.importFromExcel(file);
        return ResponseEntity.ok(imported);
    }
}