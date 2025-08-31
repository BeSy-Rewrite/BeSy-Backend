package de.hs_esslingen.besy.exceptions;

public class StatusNotSuccessException extends RuntimeException {
    public StatusNotSuccessException(String message) {
        super(message);
    }
}
