package org.sid.pfespring.controller;

import org.sid.pfespring.dto.RequestProfDTO;
import org.sid.pfespring.dto.ResponseProfDTO;
import org.sid.pfespring.services.ProfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/profs")
public class ProfController extends AbstractController<
        RequestProfDTO,
        ResponseProfDTO> {

    private final ProfService profService;

    public ProfController(ProfService service) {
        super(service);
        this.profService = service;
    }

    @PostMapping("/import")
    public ResponseEntity<List<ResponseProfDTO>> importExcel(
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(profService.importFromExcel(file));
    }
}