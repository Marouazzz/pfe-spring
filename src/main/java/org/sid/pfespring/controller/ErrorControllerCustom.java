package org.sid.pfespring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

//
//@Controller
//public class ErrorControllerCustom {
//
//    @GetMapping("/erreur")
//    public String erreurPage() {
//        return "erreur";
//    }
//}
@Controller
public class ErrorControllerCustom {

    @GetMapping("/erreur")
    public String erreurPage(
            @RequestParam(required = false) String message,
            Model model) {
        model.addAttribute("message",
                message != null ? message : "Vous devez compléter les étapes précédentes.");
        return "erreur";
    }
}