package org.sid.pfespring.controller;

import java.util.List;

import org.sid.pfespring.dto.ResponseAffectationCreateDTO;
import org.sid.pfespring.services.AffectationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/affectations")
public class AffectationController extends AbstractController<Object, Object>{
    
    private AffectationService service;

    

    public AffectationController(AffectationService service) {
        super(service);
        this.service = service;
    }

    @PostMapping("/prof_pfe")
    public ResponseEntity<ResponseAffectationCreateDTO> affecterProfPFE(){
        int nbrAffectation = this.service.affecterProfPFE();
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseAffectationCreateDTO(nbrAffectation ==0?"failed":"success",nbrAffectation));
    }

    @Override
    public ResponseEntity<Object> creer(Object request) {
        return super.creer(request);
    }

    @Override
    public ResponseEntity<List<Object>> listerTous() {
        return super.listerTous();
    }

    


}
