package de.hs_esslingen.besy.exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Preserves the original cause (and its stack trace) instead of
     * flattening it into a concatenated message string.
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
