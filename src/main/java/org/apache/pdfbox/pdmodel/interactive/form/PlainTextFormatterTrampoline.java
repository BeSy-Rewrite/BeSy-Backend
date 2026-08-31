package org.apache.pdfbox.pdmodel.interactive.form;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * PRODUCTION BRIDGE INTO PDFBOX INTERNALS.
 *
 * Deliberately placed in PDFBox's own package so this class gains
 * package-private access to {@code PlainTextFormatter}/{@code AppearanceStyle}/
 * {@code PlainText}, none of which are exposed as public API.
 *
 * <p>
 * <strong>MAINTENANCE WARNING:</strong> this relies entirely on PDFBox
 * 3.5.3's INTERNAL, UNDOCUMENTED implementation. A future PDFBox version
 * upgrade is very likely to require this class to be re-verified or
 * adjusted -- expect a COMPILE ERROR (safe failure mode) rather than a
 * silent behavior change, since this is real Java code in that package,
 * not reflection. See README.md, section "PDFBox internal API usage", for
 * the full rationale and the diagnostic history that led to this design
 * (see {@code FontEmbeddingDiagnostic} in the test sources for the
 * investigation that ruled out every documented alternative).
 *
 * <p>
 * The key property this exploits: {@link AppearanceStyle#getFont()} is
 * asked directly for a font object -- {@code PlainTextFormatter} never
 * re-resolves it from {@code PDResources}/{@code /DR} by name. That
 * sidesteps a confirmed PDFBox defect where re-resolving certain embedded
 * CID-keyed fonts (e.g. Noto Sans SC) from the AcroForm's default
 * resources silently substitutes a fallback font lacking the needed
 * glyphs.
 *
 * <p>
 * <strong>{@code beginText()}/{@code endText()} contract:</strong>
 * {@link PlainTextFormatter#format()} expects to be called while the
 * content stream is already inside an active text object -- it does not
 * open or close one itself (mirroring how PDFBox's own
 * {@code AppearanceGeneratorHelper} calls it internally). This method
 * owns that lifecycle so callers don't need to know about it.
 *
 * <p>
 * <strong>Subsetting contract:</strong> {@link PDAppearanceContentStream}
 * never registers the font it draws with for automatic subsetting at
 * {@code PDDocument.save()} time (it passes a {@code null} document
 * reference internally). Callers of this method MUST call
 * {@code ((PDType0Font) font).subset()} themselves after all drawing into
 * this appearance stream is complete and before {@code document.save()},
 * or the embedded font will end up with no glyph data at all.
 */
public final class PlainTextFormatterTrampoline {

    private PlainTextFormatterTrampoline() {
    }

    public static void formatIntoAppearanceStream(
            PDAppearanceContentStream contentStream,
            PDFont font,
            float fontSize,
            String text,
            boolean wrapLines,
            float width) throws IOException {

        AppearanceStyle style = new AppearanceStyle() {
            @Override
            public PDFont getFont() {
                return font; // OUR instance -- never re-resolved from /DR
            }

            @Override
            public float getFontSize() {
                return fontSize;
            }
        };

        PlainTextFormatter formatter = new PlainTextFormatter.Builder(contentStream)
                .style(style)
                .text(new PlainText(text))
                .wrapLines(wrapLines)
                .width(width)
                .build();

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        formatter.format();
        contentStream.endText();
    }
}
