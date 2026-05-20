package org.sid.pfespring.exception;

import java.util.List;

public class PFEImportValidationException extends RuntimeException{

    private List<String> errors;
    public PFEImportValidationException(List<String> errors) {
        this.errors = errors;
    }


    public List<String> getErrors(){
        return this.errors;
    }
    
}
