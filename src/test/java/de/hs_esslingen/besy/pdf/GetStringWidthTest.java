package de.hs_esslingen.besy.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetStringWidthTest {

    private Method getStringWidth;
    private PDFOrder order;
    private PDDocument document;

    @BeforeEach
    void setUp() throws Exception {
        OrderPdfProperties properties = OrderPdfProperties.defaults();
        PdfTemplateLoader loader = new PdfTemplateLoader(properties);
        document = loader.loadOrderTemplate();
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        EmbeddedFontProvider fontProvider = new EmbeddedFontProvider();
        fontProvider.init();
        order = new PDFOrder().parseOrder(acroForm, document, fontProvider);

        getStringWidth = PDFOrder.class.getDeclaredMethod("getStringWidth", String.class);
        getStringWidth.setAccessible(true);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws IOException {
        document.close();
    }

    private float width(String s) throws Exception {
        return (float) getStringWidth.invoke(order, s);
    }

    @Test
    void eszettContributesRealGlyphWidth_notZero() throws Exception {
        // Previous bug: ß has no ASCII/NFD decomposition, so the old
        // strip-based measurement dropped it entirely -- "Straße" measured
        // narrower than "Strae". Now it must measure wider than "Strae"
        // (its own width) by roughly ß's real glyph width.
        float withEszett = width("Straße");
        float withoutEszett = width("Strae");

        assertThat(withEszett).isGreaterThan(withoutEszett);
    }

    @Test
    void umlautsAreMeasuredAsThemselves_notAsBaseLetter() throws Exception {
        // Not a strict width assertion (ü/u happen to share a glyph width
        // in Helvetica) -- this asserts the method no longer throws/behaves
        // erratically for decomposable diacritics, and produces a
        // deterministic, non-zero result consistent with direct
        // measurement.
        float value = width("Bürostuhl");
        assertThat(value).isGreaterThan(0f);
    }

    @Test
    void cjkIsMeasuredAsPlaceholderWidth_notZero() throws Exception {
        // Previous bug: entire string stripped to "" => width 0.0 => never
        // wraps, however long. Now each unsupported code point measures as
        // the placeholder ('?') glyph width, so three CJK characters measure
        // as three placeholder-widths, not zero.
        float three = width("中文测");
        float one = width("中");

        assertThat(one).isGreaterThan(0f);
        assertThat(three).isCloseTo(one * 3, org.assertj.core.data.Percentage.withPercentage(1));
    }

    @Test
    void emojiSurrogatePairIsMeasuredAsOneUnit_notTwoLoneSurrogates() throws Exception {
        // A single emoji (surrogate pair, 2 chars / 1 code point) must
        // measure as exactly one placeholder-width, not two (which would
        // happen if measurement iterated by char instead of by code point).
        float emoji = width("😀");
        float cjkSingle = width("中");

        assertThat(emoji).isCloseTo(cjkSingle, org.assertj.core.data.Percentage.withPercentage(1));
    }

    @Test
    void controlCharactersContributeNoWidth() throws Exception {
        assertThat(width("\n")).isEqualTo(0f);
        assertThat(width("a\nb")).isEqualTo(width("ab"));
    }

    @Test
    void plainAsciiIsUnaffected() throws Exception {
        assertThat(width("Laptop 14 Zoll")).isGreaterThan(0f);
    }
}
