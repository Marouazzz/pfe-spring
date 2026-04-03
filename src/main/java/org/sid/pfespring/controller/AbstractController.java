package org.sid.pfespring.controller;

import org.sid.pfespring.services.GenericService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class AbstractController<Req, Res> {

    protected final GenericService<Req, Res> service;

    protected AbstractController(GenericService<Req, Res> service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Res> creer(@RequestBody Req request) {
        return ResponseEntity.ok(service.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<Res>> listerTous() {
        return ResponseEntity.ok(service.listerTous());
    }
}