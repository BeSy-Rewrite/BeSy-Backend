package de.hs_esslingen.besy.pdf;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import de.hs_esslingen.besy.models.Vat;

/**
 * Numeric values calculated for an order PDF.
 *
 * <p>
 * The VAT set deliberately contains {@link Vat} entities rather than VAT
 * values. Vat currently uses identity-based equality, and changing that would
 * alter the frozen mixed-VAT behaviour.
 *
 * <p>
 * Total and VAT value are empty for the existing multi-VAT path, where the
 * respective PDF fields remain untouched.
 */
public record OrderPdfTotals(
        BigDecimal subTotal,
        BigDecimal netTotal,
        Optional<BigDecimal> total,
        Optional<BigDecimal> vatValue,
        Set<Vat> vats) {
}
