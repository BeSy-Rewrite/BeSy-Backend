package de.hs_esslingen.besy.pdf;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText;

/**
 * Writes String values into AcroForm fields without ever throwing on
 * characters the field's font cannot encode.
 *
 * <p>
 * PDFBox's {@link PDField#setValue(String)} throws an unchecked
 * {@link IllegalArgumentException} deep inside appearance-stream generation
 * whenever the value contains a code point the field's DA font cannot
 * encode (see {@code AppearanceGeneratorHelper}). Every value written into
 * this order PDF ultimately originates from user/DB input (supplier names,
 * item descriptions, comments, ...), so an emoji, CJK character, or any
 * other glyph outside the template's WinAnsi-only fonts (ArialMT, Calibri,
 * Calibri-Bold, Helvetica) would otherwise crash the entire PDF generation.
 *
 * <p>
 * This class is a defensive last-resort net: it replaces individual
 * unsupported code points with {@link #PLACEHOLDER} <em>before</em> calling
 * {@code setValue}, so generation never crashes. It intentionally operates
 * per Unicode code point (not per UTF-16 {@code char}), so a surrogate pair
 * (e.g. an emoji) is replaced as a whole rather than splitting it into two
 * lone surrogates.
 *
 * <p>
 * <strong>Control characters are never sanitized.</strong> PDFBox's
 * {@code PlainTextFormatter} splits multi-line field values on {@code \n} /
 * {@code \r} / {@code \r\n} <em>before</em> looking up any glyph — line
 * separators are structural and are never actually encoded against the
 * font. Running them through {@link PDFont#encode(String)} anyway (as an
 * earlier version of this class did) incorrectly reports them as
 * unsupported and replaces them with {@code '?'}, silently destroying
 * multi-line values such as a supplier's multi-line address.
 *
 * <p>
 * This is a stopgap, not the primary fix for Unicode fidelity — a Unicode-
 * capable fallback font (embedded at runtime) is planned separately for
 * scripts genuinely outside WinAnsi. This class remains the safety net for
 * whatever that fallback still can't cover.
 */
public final class PdfSafeFieldWriter {

    static final char PLACEHOLDER = '?';

    private static final Pattern FONT_NAME_PATTERN = Pattern.compile("/(\\S+)\\s+[-\\d.]+\\s+Tf");

    private final PDResources defaultResources;
    private final String acroFormDefaultDA;
    private final Map<PDFont, Set<Integer>> unsupportedCodePointsByFont = new HashMap<>();

    PdfSafeFieldWriter(PDAcroForm acroForm) {
        this.defaultResources = acroForm.getDefaultResources();
        this.acroFormDefaultDA = acroForm.getDefaultAppearance();
    }

    /**
     * Sanitizes {@code value} against the field's actual font, then calls
     * {@link PDField#setValue(String)}. {@code null} is treated as {@code ""}.
     */
    void setValue(PDField field, String value) throws IOException {
        String safeValue = value != null ? value : "";
        field.setValue(sanitize(field, safeValue));
    }

    private String sanitize(PDField field, String value) {
        PDFont font = resolveFont(field);
        if (font == null) {
            // Font could not be resolved -> nothing we can verify against;
            // pass the value through unchanged (matches pre-existing behavior,
            // no new risk introduced).
            return value;
        }

        Set<Integer> unsupported = unsupportedCodePointsByFont.computeIfAbsent(font, f -> new HashSet<>());
        StringBuilder result = new StringBuilder(value.length());
        value.codePoints().forEach(codePoint -> {
            if (isEncodable(font, codePoint, unsupported)) {
                result.appendCodePoint(codePoint);
            } else {
                result.append(PLACEHOLDER);
            }
        });
        return result.toString();
    }

    private boolean isEncodable(PDFont font, int codePoint, Set<Integer> unsupportedCache) {
        // Structural line-break/control characters (\n, \r, \t, ...) are
        // handled by PDFBox's line-splitting logic before any glyph lookup
        // happens -- they are never actually encoded against the font, so
        // they must never be sanitized (see class Javadoc).
        if (Character.isISOControl(codePoint)) {
            return true;
        }

        if (unsupportedCache.contains(codePoint)) {
            return false;
        }
        try {
            font.encode(new String(Character.toChars(codePoint)));
            return true;
        } catch (IOException | IllegalArgumentException e) {
            unsupportedCache.add(codePoint);
            return false;
        }
    }

    private PDFont resolveFont(PDField field) {
        if (defaultResources == null || !(field instanceof PDVariableText variableTextField)) {
            return null;
        }
        String da = variableTextField.getDefaultAppearance();
        if (da == null || da.isBlank()) {
            da = acroFormDefaultDA;
        }
        String fontResourceName = extractFontName(da);
        if (fontResourceName == null) {
            return null;
        }
        try {
            return defaultResources.getFont(COSName.getPDFName(fontResourceName));
        } catch (IOException e) {
            return null;
        }
    }

    private String extractFontName(String da) {
        if (da == null) {
            return null;
        }
        Matcher m = FONT_NAME_PATTERN.matcher(da);
        return m.find() ? m.group(1) : null;
    }
}
