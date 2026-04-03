package org.sid.pfespring.controller;


import org.sid.pfespring.dto.ProfDTO;
import org.sid.pfespring.services.ProfService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/professeurs")
public class ProfController {
    private final ProfService profService;

    public ProfController(ProfService professeurService) {
        this.profService = professeurService;
    }

    @PostMapping("/import")
    public ResponseEntity<List<ProfDTO.Response>> importerExcel(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<ProfDTO.Response> imported = profService.importFromExcel(file);
        return ResponseEntity.ok(imported);
    }



}
