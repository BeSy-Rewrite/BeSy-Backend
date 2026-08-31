package de.hs_esslingen.besy.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FontCandidateSelectorTest {

    private PDDocument document;
    private FontCandidateSelector selector;

    @BeforeEach
    void setUp() throws IOException {
        document = new PDDocument();
        EmbeddedFontProvider provider = new EmbeddedFontProvider();
        provider.init();
        selector = new FontCandidateSelector(provider);
    }

    @AfterEach
    void tearDown() throws IOException {
        document.close();
    }

    @Test
    void pureLatinExtended_selectsRegularNotoSans_zeroUnsupported() throws IOException {
        var selection = selector.selectBestFont(document, "Müller Straße é ñ", false);
        assertThat(selection.unsupportedCodePointCount()).isZero();
    }

    @Test
    void pureCjk_selectsCjkFont_zeroUnsupported() throws IOException {
        var selection = selector.selectBestFont(document, "中文测", false);
        assertThat(selection.unsupportedCodePointCount()).isZero();
    }

    @Test
    void mixedLatinAndCjk_selectsCjkFont_sinceItAlsoCoversLatin() throws IOException {
        var selection = selector.selectBestFont(document, "Müller 中文 GmbH", false);
        assertThat(selection.unsupportedCodePointCount()).isZero();
    }

    @Test
    void pureEmoji_selectsEmojiFont_zeroUnsupported() throws IOException {
        var selection = selector.selectBestFont(document, "😀😀😀", false);
        assertThat(selection.unsupportedCodePointCount()).isZero();
    }

    @Test
    void mixedLatinAndEmoji_picksFontWithFewestUnsupported_notNecessarilyZero() throws IOException {
        // Neither NotoSans (no emoji) nor NotoEmoji (no Latin) covers this
        // fully -- exactly the accepted limitation from our design discussion.
        var selection = selector.selectBestFont(document, "Müller 😀 GmbH", false);
        assertThat(selection.unsupportedCodePointCount()).isGreaterThan(0);
    }
}
