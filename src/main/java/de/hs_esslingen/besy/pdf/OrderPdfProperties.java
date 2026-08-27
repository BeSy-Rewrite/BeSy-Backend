package de.hs_esslingen.besy.pdf;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Configurable defaults for the order PDF, previously the {@code *_DEFAULT}
 * constants in {@link OrderPDFService}.
 *
 * <p>
 * The default values are kept in Java (not in application.properties) for two
 * reasons: they must stay byte-identical to the frozen snapshots, and
 * {@code defaultStreet} contains a non-ASCII character whose encoding depends
 * on
 * the properties file charset. Override via {@code besy.pdf.order.*} only if a
 * snapshot update is intended.
 */
@Component
@ConfigurationProperties(prefix = "besy.pdf.order")
@Getter
@Setter
public class OrderPdfProperties {

    private String templateLocation = "static/Bestellformular_V01_empty.pdf";

    private String defaultFaculty = "IT";

    /**
     * Currently unused (was ANSCHRIFT_STRASSE_DEFAULT); kept for parity, removal
     * candidate in later steps.
     */
    private String defaultStreet = "Flandernstraße 101";

    /**
     * Currently unused (was ANSCHRIFT_PLZ_ORT_DEFAULT); kept for parity, removal
     * candidate in later steps.
     */
    private String defaultPostalAndTown = "73732 Esslingen";

    private String defaultLfdNr = "1";

    /**
     * Fallback VAT rate used when an order has no items with a VAT rate at
     * all. A {@code BigDecimal} now — no more string-parsing detour in
     * the calculator.
     */
    private BigDecimal defaultVatRate = new BigDecimal("19");

    /**
     * Production defaults, for tests that construct the service without a Spring
     * context.
     */
    public static OrderPdfProperties defaults() {
        return new OrderPdfProperties();
    }
}
