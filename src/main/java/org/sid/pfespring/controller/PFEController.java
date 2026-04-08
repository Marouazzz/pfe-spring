package org.sid.pfespring.controller;

import java.util.List;

import org.sid.pfespring.dto.RequestPFEDTO;
import org.sid.pfespring.dto.ResponsePFEDTO;
import org.sid.pfespring.services.PFEService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/pfes")
public class PFEController extends AbstractController<RequestPFEDTO, ResponsePFEDTO>{

    PFEService pFEService;

    public PFEController(PFEService service) {
        super(service);
        this.pFEService = service;
    }

    @PostMapping("/import")
    public ResponseEntity<List<ResponsePFEDTO>> importFromExcel(@RequestParam("file") MultipartFile file){
        List<ResponsePFEDTO> pfes = pFEService.importFromExcel(file);
        return ResponseEntity.status(HttpStatus.CREATED)
        .body(pfes);
    }

    @PostMapping("/affectations")
    public ResponseEntity<Integer> affecterProfPFE(){
        return ResponseEntity.status(HttpStatus.CREATED).body(pFEService.affecterProfPFE());
    }



    


    
}
