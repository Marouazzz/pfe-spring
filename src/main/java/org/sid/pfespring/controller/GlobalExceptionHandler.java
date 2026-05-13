package org.sid.pfespring.controller;


import org.sid.pfespring.exception.BusinessException;
import org.sid.pfespring.exception.EtudiantImportValidationException;
import org.sid.pfespring.exception.EtudiantNotFoundException;
import org.sid.pfespring.exception.ProfImportValidationException;
import org.sid.pfespring.exception.PFEImportValidationException;
import org.sid.pfespring.exception.InvalidSheetStructureException;
import org.sid.pfespring.exception.NotSupportedLanguageException;
//import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(EtudiantImportValidationException.class)
//    public ResponseEntity<String> handleEtudiantImport(EtudiantImportValidationException e){
//        return ResponseEntity.badRequest().body("Le sheet 'etu' contient des erreurs de validations sur les lignes suivantes \n" + String.join("\n",e.getErrors()));
//    }
//
//    @ExceptionHandler(ProfImportValidationException.class)
//    public ResponseEntity<String> handleProfImport(ProfImportValidationException e){
//        return ResponseEntity.badRequest().body("Le sheet 'profs' contient des erreurs de validations sur les lignes suivantes \n" + String.join("\n",e.getErrors()));
//    }
//
//    @ExceptionHandler(PFEImportValidationException.class)
//    public ResponseEntity<String> handlePFEImport(PFEImportValidationException e){
//        return ResponseEntity.badRequest().body("Le sheet 'pfe' contient des erreurs de validations sur les lignes suivantes \n" + String.join("\n",e.getErrors()));
//    }
//
//    @ExceptionHandler(InvalidSheetStructureException.class)
//    public ResponseEntity<String> handleSheetStructure(InvalidSheetStructureException e){
//        return ResponseEntity.badRequest().body(e.getMessage());
//    }
//
//    @ExceptionHandler(EtudiantNotFoundException.class)
//    public ResponseEntity<String> handleEtudiantNotFound(EtudiantNotFoundException e){
//        return ResponseEntity.badRequest().body(e.getMessage());
//    }
//
//    /// metier csad par user
//    @ExceptionHandler(BusinessException.class)
//    public ResponseEntity<String> handleBusiness(BusinessException e) {
//        return ResponseEntity.badRequest().body(e.getMessage());
//    }
//    /// technique
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<String> handleRuntime(RuntimeException e) {
//        return ResponseEntity.internalServerError().body(e.getMessage());
//    }


    @ExceptionHandler(NotSupportedLanguageException.class)
    public String handleLnaguageException(NotSupportedLanguageException e,RedirectAttributes ra){
        ra.addAttribute("message",e.getMessage());
        return "redirect:/erreur";
    }
    @ExceptionHandler(EtudiantImportValidationException.class)
    public String handleEtudiantImport(EtudiantImportValidationException e, RedirectAttributes ra) {
        ra.addAttribute("message", "Le sheet 'etu' contient des erreurs : " + String.join(" | ", e.getErrors()));
        return "redirect:/erreur";
    }

    @ExceptionHandler(ProfImportValidationException.class)
    public String handleProfImport(ProfImportValidationException e, RedirectAttributes ra) {
        ra.addAttribute("message", "Le sheet 'profs' contient des erreurs : " + String.join(" | ", e.getErrors()));
        return "redirect:/erreur";
    }

    @ExceptionHandler(PFEImportValidationException.class)
    public String handlePFEImport(PFEImportValidationException e, RedirectAttributes ra) {
        ra.addAttribute("message", "Le sheet 'pfes' contient des erreurs : " + String.join(" | ", e.getErrors()));
        return "redirect:/erreur";
    }

    @ExceptionHandler(InvalidSheetStructureException.class)
    public String handleSheetStructure(InvalidSheetStructureException e, RedirectAttributes ra) {
        ra.addAttribute("message", e.getMessage());
        return "redirect:/erreur";
    }

    @ExceptionHandler(EtudiantNotFoundException.class)
    public String handleEtudiantNotFound(EtudiantNotFoundException e, RedirectAttributes ra) {
        ra.addAttribute("message", e.getMessage());
        return "redirect:/erreur";
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException e, RedirectAttributes ra) {
        ra.addAttribute("message", e.getMessage());
        return "redirect:/erreur";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntime(RuntimeException e, RedirectAttributes ra) {
        ra.addAttribute("message", "Erreur technique : " + e.getMessage());
        return "redirect:/erreur";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleArgumentException(IllegalArgumentException e ,RedirectAttributes ra){
        ra.addAttribute("message",e.getMessage());
        return "redirect:/erreur";
    }
}

