package org.sid.pfespring.exception;

public class InvalidSheetStructureException extends RuntimeException {

    public InvalidSheetStructureException(String message) {
        super(message);
    }

    public InvalidSheetStructureException(Throwable cause) {
        super(cause);
    }

    public InvalidSheetStructureException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSheetStructureException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
