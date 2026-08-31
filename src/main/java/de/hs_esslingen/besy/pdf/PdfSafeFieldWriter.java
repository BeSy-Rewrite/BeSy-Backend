package de.hs_esslingen.besy.pdf;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText;
import org.apache.pdfbox.pdmodel.interactive.form.PlainTextFormatterTrampoline;

/**
 * Writes String values into AcroForm fields, choosing between two paths:
 *
 * <ol>
 * <li><strong>Fast path</strong> (the common case): if the field's own
 * template font (ArialMT/Calibri/Calibri-Bold/Helvetica -- all WinAnsi-only)
 * can already encode every character in the value, delegate to
 * {@link PDField#setValue(String)} unchanged. Zero behavior change from
 * before this class grew fallback-font support.</li>
 * <li><strong>Fallback path</strong> (only when the template font can't
 * cover something -- e.g. CJK, emoji, Cyrillic beyond Calibri's Latin
 * Extended): pick whichever embedded fallback font
 * ({@link EmbeddedFontProvider}) covers the most of the value via
 * {@link FontCandidateSelector}, sanitize whatever even that font can't
 * cover to {@link #PLACEHOLDER}, and render the field's appearance
 * ourselves via a manually built {@link PDAppearanceStream} plus
 * {@link PlainTextFormatterTrampoline} (reusing PDFBox's real line-wrapping
 * engine). This bypasses {@link PDField#setValue(String)} entirely for
 * this value, because PDFBox's automatic appearance generation
 * (re-resolving the font by name from {@code /DR}) silently substitutes a
 * broken fallback font for certain embedded CID-keyed fonts (confirmed via
 * {@code FontEmbeddingDiagnostic} -- see README.md, "PDFBox internal API
 * usage").</li>
 * </ol>
 *
 * <p>
 * Both paths operate per Unicode code point (not per UTF-16 {@code char}),
 * so a surrogate pair (e.g. an emoji) is always treated as one unit.
 *
 * <p>
 * <strong>Control characters are never sanitized.</strong> PDFBox's
 * {@code PlainTextFormatter} (and the automatic appearance generator) split
 * multi-line field values on {@code \n}/{@code \r}/{@code \r\n}
 * <em>before</em> looking up any glyph -- line separators are structural
 * and are never actually encoded against a font.
 *
 * <p>
 * <strong>Accepted limitation:</strong> this does not attempt real mixed-
 * script rendering. A value mixing genuinely disjoint scripts that no
 * single embedded font covers together (e.g. Latin text plus an emoji,
 * since the emoji font has no Latin coverage) picks whichever single font
 * covers the most of the value; the remainder still becomes
 * {@link #PLACEHOLDER}. See README.md for the accepted scope.
 */
public final class PdfSafeFieldWriter {

    static final char PLACEHOLDER = '?';

    private static final Pattern FONT_NAME_AND_SIZE_PATTERN = Pattern.compile("/(\\S+)\\s+([-\\d.]+)\\s+Tf");
    private static final float DEFAULT_FONT_SIZE = 12f;
    private static final float APPEARANCE_WIDTH_MARGIN = 4f;

    private final PDDocument document;
    private final PDResources defaultResources;
    private final String acroFormDefaultDA;
    private final FontCandidateSelector fontSelector;
    private final Map<PDFont, Set<Integer>> unsupportedCodePointsByFont = new HashMap<>();

    PdfSafeFieldWriter(PDDocument document, PDAcroForm acroForm, EmbeddedFontProvider fontProvider) {
        this.document = document;
        this.defaultResources = acroForm.getDefaultResources();
        this.acroFormDefaultDA = acroForm.getDefaultAppearance();
        this.fontSelector = new FontCandidateSelector(fontProvider);
    }

    /**
     * Writes {@code value} into {@code field}, choosing the fast or
     * fallback path described in the class Javadoc. {@code null} is
     * treated as {@code ""}.
     */
    void setValue(PDField field, String value) throws IOException {
        String safeValue = value != null ? value : "";

        String da = resolveDA(field);
        PDFont originalFont = resolveFont(da);

        if (originalFont == null) {
            // Font could not be resolved -> nothing we can verify against;
            // fall back to plain setValue(), matching this class's
            // pre-fallback-font behavior (no new risk introduced).
            field.setValue(safeValue);
            return;
        }

        if (isFullyEncodable(originalFont, safeValue)) {
            field.setValue(safeValue);
            return;
        }

        if (!canRenderManually(field)) {
            // Defensive: this writer is only ever called with single-widget
            // PDTextFields in this template. If that assumption ever
            // breaks, sanitize against the ORIGINAL font and use the
            // normal setValue() path, rather than attempting (and failing)
            // the manual-appearance path below.
            field.setValue(sanitizeAgainst(originalFont, safeValue));
            return;
        }

        boolean bold = isBoldFontName(extractFontName(da));
        FontCandidateSelector.Selection selection = fontSelector.selectBestFont(document, safeValue, bold);
        String sanitized = sanitizeAgainst(selection.font(), safeValue);
        float fontSize = extractFontSize(da);

        writeManualAppearance((PDTextField) field, selection.font(), fontSize, sanitized);
    }

    private boolean canRenderManually(PDField field) {
        return field instanceof PDTextField textField && !textField.getWidgets().isEmpty();
    }

    // ------------------------------------------------------------- Fast-path
    // checks

    private boolean isFullyEncodable(PDFont font, String value) {
        Set<Integer> unsupported = unsupportedCodePointsByFont.computeIfAbsent(font, f -> new HashSet<>());
        return value.codePoints().allMatch(codePoint -> isEncodable(font, codePoint, unsupported));
    }

    private String sanitizeAgainst(PDFont font, String value) {
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

    // ------------------------------------------------------------- Fallback path

    /**
     * Renders the field's appearance ourselves and sets the raw {@code /V}
     * value directly, bypassing {@link PDField#setValue(String)} entirely.
     *
     * <p>
     * Chooses single-line or wrapped multi-line layout based on the
     * field's own {@code Ff} multiline flag ({@link PDTextField#isMultiline()}),
     * so a fallback-font value in a multi-line comment field still wraps,
     * while a fallback-font value in a naturally single-line field (e.g. a
     * name/address line) does not.
     */
    private void writeManualAppearance(PDTextField textField, PDFont font, float fontSize, String text)
            throws IOException {
        PDAnnotationWidget widget = textField.getWidgets().get(0);
        PDRectangle rect = widget.getRectangle();

        PDAppearanceStream appearance = new PDAppearanceStream(document);
        PDResources apResources = new PDResources();
        apResources.put(COSName.getPDFName("F1"), font);
        appearance.setResources(apResources);
        appearance.setBBox(new PDRectangle(rect.getWidth(), rect.getHeight()));

        boolean wrapLines = textField.isMultiline();
        float availableWidth = Math.max(0f, rect.getWidth() - APPEARANCE_WIDTH_MARGIN);

        try (PDAppearanceContentStream cs = new PDAppearanceContentStream(appearance)) {
            PlainTextFormatterTrampoline.formatIntoAppearanceStream(
                    cs, font, fontSize, text, wrapLines, availableWidth);
        }

        // PDAppearanceContentStream never registers the font with the
        // PDDocument for automatic subsetting at save() time -- see
        // PlainTextFormatterTrampoline's class Javadoc. Do it ourselves.
        if (font instanceof PDType0Font type0Font) {
            type0Font.subset();
        }

        PDAppearanceDictionary appearanceDict = widget.getAppearance();
        if (appearanceDict == null) {
            appearanceDict = new PDAppearanceDictionary();
            widget.setAppearance(appearanceDict);
        }
        appearanceDict.setNormalAppearance(appearance);

        textField.getCOSObject().setString(COSName.V, text);
    }

    // ------------------------------------------------------------- Font/DA
    // resolution

    private String resolveDA(PDField field) {
        if (!(field instanceof PDVariableText variableTextField)) {
            return acroFormDefaultDA;
        }
        String da = variableTextField.getDefaultAppearance();
        return (da == null || da.isBlank()) ? acroFormDefaultDA : da;
    }

    private PDFont resolveFont(String da) {
        if (defaultResources == null) {
            return null;
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
        Matcher m = FONT_NAME_AND_SIZE_PATTERN.matcher(da);
        return m.find() ? m.group(1) : null;
    }

    private float extractFontSize(String da) {
        if (da != null) {
            Matcher m = FONT_NAME_AND_SIZE_PATTERN.matcher(da);
            if (m.find()) {
                try {
                    return Float.parseFloat(m.group(2));
                } catch (NumberFormatException ignored) {
                    // fall through to default
                }
            }
        }
        return DEFAULT_FONT_SIZE;
    }

    private boolean isBoldFontName(String fontResourceName) {
        return fontResourceName != null && fontResourceName.toLowerCase(Locale.ROOT).contains("bold");
    }
}
