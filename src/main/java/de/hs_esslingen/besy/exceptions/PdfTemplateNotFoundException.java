package de.hs_esslingen.besy.exceptions;

import java.io.FileNotFoundException;

/**
 * Thrown when the order PDF AcroForm template cannot be found on the
 * classpath.
 *
 * <p>
 * Extends {@link FileNotFoundException} on purpose: callers up to
 * {@code OrderController} already declare {@code throws IOException}, so
 * introducing this type is signature-neutral and existing
 * {@code FileNotFoundException} handling keeps working.
 *
 * <p>
 * This is a configuration/deployment fault (template missing from the jar or
 * {@code besy.pdf.order.template-location} misconfigured), not a client error —
 * see {@link GlobalExceptionHandler} for the 500 mapping.
 */
public class PdfTemplateNotFoundException extends FileNotFoundException {

    private final String location;

    public PdfTemplateNotFoundException(String location) {
        super("Order PDF template not found at classpath: " + location);
        this.location = location;
    }

    /** The classpath location that was searched. */
    public String getLocation() {
        return location;
    }
}
