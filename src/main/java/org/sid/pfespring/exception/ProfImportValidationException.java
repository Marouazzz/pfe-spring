package org.sid.pfespring.exception;
import java.util.List;


public class ProfImportValidationException extends RuntimeException {

    private List<String> errors;
    public ProfImportValidationException(List<String> errors) {
        this.errors = errors;
    }


    public List<String> getErrors(){
        return this.errors;
    }
    
}
