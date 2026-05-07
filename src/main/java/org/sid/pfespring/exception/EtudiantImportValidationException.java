package org.sid.pfespring.exception;

import java.util.List;


public class EtudiantImportValidationException extends RuntimeException {

    private List<String> errors;
    public EtudiantImportValidationException(List<String> errors) {
        this.errors = errors;
    }


    public List<String> getErrors(){
        return this.errors;
    }
    
}
