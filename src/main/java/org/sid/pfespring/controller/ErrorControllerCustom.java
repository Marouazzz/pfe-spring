package org.sid.pfespring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorControllerCustom {

    @GetMapping("/erreur")
    public String erreurPage() {
        return "erreur";
    }
}