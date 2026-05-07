package org.sid.pfespring.exception;

public class EtudiantNotFoundException extends RuntimeException{

    public EtudiantNotFoundException() {
    }

    public EtudiantNotFoundException(String message) {
        super(message);
    }

    public EtudiantNotFoundException(Throwable cause) {
        super(cause);
    }

    public EtudiantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EtudiantNotFoundException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
