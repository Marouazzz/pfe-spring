package org.sid.pfespring.controller;


import org.sid.pfespring.exception.BusinessException;
import org.sid.pfespring.exception.EtudiantImportValidationException;
import org.sid.pfespring.exception.EtudiantNotFoundException;
import org.sid.pfespring.exception.InvalidSheetStructureException;
import org.sid.pfespring.exception.NotSupportedLanguageException;
import org.sid.pfespring.exception.PFEImportValidationException;
import org.sid.pfespring.exception.ProfImportValidationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {
    
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

