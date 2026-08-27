package de.hs_esslingen.besy.pdf;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.Objects;

/**
 * Single place for all value-to-string conversions used in the order PDF.
 *
 * <p>
 * Amounts are rendered with exactly 2 decimals (HALF_UP) and a German
 * thousands separator, e.g. {@code 2400} -> {@code "2.400,00 €"}.
 *
 * <p>
 * <strong>Null handling:</strong> {@link #formatDecimal(BigDecimal)} and
 * {@link #formatCurrency(BigDecimal)} throw a {@link NullPointerException}
 * on {@code null} rather than silently rendering an empty/default value. A
 * {@code null} amount here means a caller forgot to default a business-null
 * (e.g. "no discount entered") — that is a caller bug and should fail fast
 * rather than produce a silently wrong PDF. Callers remain responsible for
 * defaulting such business-nulls before calling into this formatter.
 */
public final class PdfValueFormatter {

    private static final String CURRENCY_SUFFIX = " €";

    private PdfValueFormatter() {
    }

    /** e.g. {@code 2400} -> {@code "2.400,00 €"}. */
    public static String formatCurrency(BigDecimal amount) {
        return formatDecimal(amount).concat(CURRENCY_SUFFIX);
    }

    /**
     * German-formatted plain decimal: 2 decimals, HALF_UP, thousands
     * separator — used directly for the discount field and as the basis for
     * {@link #formatCurrency(BigDecimal)}.
     *
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public static String formatDecimal(BigDecimal value) {
        Objects.requireNonNull(value, "value must not be null");
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.GERMANY));
        return format.format(value.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * VAT rate as a plain, non-truncated number: trailing zeros are stripped,
     * so a whole-percent rate like {@code 19.00} renders as {@code "19"} and
     * a fractional rate like {@code 7.90} renders as {@code "7,9"} — the same
     * representation as {@link #formatPercentage(BigDecimal)}, just without
     * the percent sign.
     */
    public static String formatVatRate(BigDecimal vatValue) {
        Objects.requireNonNull(vatValue, "vatValue must not be null");
        return plainRate(vatValue);
    }

    /**
     * Same rate representation as {@link #formatVatRate(BigDecimal)}, with a
     * percent sign.
     */
    public static String formatPercentage(BigDecimal value) {
        Objects.requireNonNull(value, "value must not be null");
        return plainRate(value) + "%";
    }

    private static String plainRate(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    /** Medium localised date, as produced by the previous inline formatter. */
    public static String formatDate(TemporalAccessor date, Locale locale) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(date);
    }
}
