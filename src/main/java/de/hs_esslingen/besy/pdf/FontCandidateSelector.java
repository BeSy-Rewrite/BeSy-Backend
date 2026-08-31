package de.hs_esslingen.besy.pdf;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * Chooses, among a small set of candidate embedded fonts, the one that can
 * render a given piece of text with the fewest unsupported ("would need a
 * placeholder") code points.
 *
 * <p>
 * Deliberately simple: this does NOT attempt real mixed-script rendering
 * (splitting a string into per-script runs, each drawn with a different
 * font side by side). A value mixing, e.g., Latin text and an emoji picks
 * whichever single font covers the most of it; the rest is later replaced
 * with a placeholder by the caller. See README.md for the accepted scope
 * of this limitation.
 */
public final class FontCandidateSelector {

    private final EmbeddedFontProvider fontProvider;

    public FontCandidateSelector(EmbeddedFontProvider fontProvider) {
        this.fontProvider = fontProvider;
    }

    public record Selection(PDFont font, int unsupportedCodePointCount) {
    }

    /**
     * @param bold whether the Latin/Greek/Cyrillic candidate should be the
     *             bold weight, matching the field's original template font
     */
    public Selection selectBestFont(PDDocument document, String text, boolean bold) throws IOException {
        PDFont latin = bold ? fontProvider.embedBold(document) : fontProvider.embedRegular(document);
        int latinUnsupported = countUnsupported(latin, text);
        if (latinUnsupported == 0) {
            return new Selection(latin, 0);
        }

        PDFont cjk = fontProvider.embedCjk(document);
        int cjkUnsupported = countUnsupported(cjk, text);

        PDFont emoji = fontProvider.embedEmoji(document);
        int emojiUnsupported = countUnsupported(emoji, text);

        // Ties favor Latin, since it is already our baseline selection above.
        Selection best = new Selection(latin, latinUnsupported);
        if (cjkUnsupported < best.unsupportedCodePointCount()) {
            best = new Selection(cjk, cjkUnsupported);
        }
        if (emojiUnsupported < best.unsupportedCodePointCount()) {
            best = new Selection(emoji, emojiUnsupported);
        }
        return best;
    }

    private int countUnsupported(PDFont font, String text) {
        int count = 0;
        for (int codePoint : text.codePoints().toArray()) {
            if (Character.isISOControl(codePoint)) {
                continue;
            }
            try {
                font.encode(new String(Character.toChars(codePoint)));
            } catch (IOException | IllegalArgumentException e) {
                count++;
            }
        }
        return count;
    }
}
