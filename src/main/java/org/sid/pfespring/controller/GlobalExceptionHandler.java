package org.sid.pfespring.controller;


import org.sid.pfespring.exception.BusinessException;
import org.sid.pfespring.exception.EtudiantImportValidationException;
import org.sid.pfespring.exception.EtudiantNotFoundException;
import org.sid.pfespring.exception.ProfImportValidationException;
import org.sid.pfespring.exception.PFEImportValidationException;
import org.sid.pfespring.exception.InvalidSheetStructureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EtudiantImportValidationException.class)
    public ResponseEntity<String> handleEtudiantImport(EtudiantImportValidationException e){
        return ResponseEntity.badRequest().body("Le sheet 'etu' contient des erreurs de validations sur les lignes suivantes \n" + String.join("\n",e.getErrors()));
    }

    @ExceptionHandler(ProfImportValidationException.class)
    public ResponseEntity<String> handleProfImport(ProfImportValidationException e){
        return ResponseEntity.badRequest().body("Le sheet 'profs' contient des erreurs de validations sur les lignes suivantes \n" + String.join("\n",e.getErrors()));
    }

    @ExceptionHandler(PFEImportValidationException.class)
    public ResponseEntity<String> handlePFEImport(PFEImportValidationException e){
        return ResponseEntity.badRequest().body("Le sheet 'pfe' contient des erreurs de validations sur les lignes suivantes \n" + String.join("\n",e.getErrors()));
    }

    @ExceptionHandler(InvalidSheetStructureException.class)
    public ResponseEntity<String> handleSheetStructure(InvalidSheetStructureException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(EtudiantNotFoundException.class)
    public ResponseEntity<String> handleEtudiantNotFound(EtudiantNotFoundException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    
/// metier csad par user
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusiness(BusinessException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
/// technique
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }

}