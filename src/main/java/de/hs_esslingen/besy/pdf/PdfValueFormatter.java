package de.hs_esslingen.besy.pdf;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

/**
 * Single place for all value-to-string conversions used in the order PDF.
 *
 * <p>
 * <strong>Frozen behaviour:</strong> amounts are rendered via
 * {@code String.valueOf(BigDecimal)} plus {@code replace('.', ',')} rather than
 * a locale-aware {@link java.text.NumberFormat}. This means the scale of the
 * incoming BigDecimal leaks into the PDF (e.g. {@code 2400.00} vs
 * {@code 2400.0}) and no thousands separator is emitted. Switching to
 * {@code setScale}/NumberFormat is scheduled for later refactoringand requires
 * a reviewed snapshot update.
 *
 * <p>
 * Null inputs are deliberately <em>not</em> special-cased for amounts: the
 * previous code produced the literal {@code "null"} there, and all current
 * call sites pass non-null values.
 */
public final class PdfValueFormatter {

    private static final String CURRENCY_SUFFIX = " €";

    private PdfValueFormatter() {
    }

    /** e.g. {@code 2400.00} -> {@code "2400,00 €"}. */
    public static String formatCurrency(BigDecimal amount) {
        return formatDecimal(amount).concat(CURRENCY_SUFFIX);
    }

    /** Plain decimal with German decimal comma, no unit — used for the discount. */
    public static String formatDecimal(BigDecimal value) {
        return String.valueOf(value).replace('.', ',');
    }

    /**
     * VAT rate for the {@code MwStSatz} field: truncated to an integer, so
     * {@code 19.00} -> {@code "19"} and {@code 7.90} -> {@code "7"}.
     */
    public static String formatVatRate(BigDecimal vatValue) {
        return String.valueOf(vatValue.intValue());
    }

    /**
     * VAT rate for the mixed-VAT comment: rounded to whole percent,
     * {@code 19.00} -> {@code "19%"}.
     */
    public static String formatPercentage(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP).toString().replace('.', ',') + "%";
    }

    /** Medium localised date, as produced by the previous inline formatter. */
    public static String formatDate(TemporalAccessor date, Locale locale) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(date);
    }
}
