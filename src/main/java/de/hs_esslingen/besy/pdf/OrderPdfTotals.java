package de.hs_esslingen.besy.pdf;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import de.hs_esslingen.besy.models.Vat;

/**
 * Numeric values calculated for an order PDF.
 *
 * <p>
 * {@code total} is always present: for a single VAT rate it is the net total
 * grossed up by that rate; for mixed VAT rates it is the sum of the per-rate
 * gross totals. {@code vatValue} is only present when a single rate
 * applies — the corresponding PDF field is left blank otherwise, since there
 * is no single rate to show.
 */
public record OrderPdfTotals(
                BigDecimal subTotal,
                BigDecimal netTotal,
                Optional<BigDecimal> total,
                Optional<BigDecimal> vatValue,
                Set<Vat> vats) {
}
