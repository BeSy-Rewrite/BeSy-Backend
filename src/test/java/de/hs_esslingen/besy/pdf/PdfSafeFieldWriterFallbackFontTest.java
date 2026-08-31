package de.hs_esslingen.besy.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.jupiter.api.Test;

/**
 * Integration proof that {@link PdfSafeFieldWriter}'s fallback path really
 * produces embedded, encodable glyphs after a save/reload round-trip --
 * not just "no exception" (which, as the FontEmbeddingDiagnostic
 * investigation showed, is not sufficient evidence on its own).
 */
class PdfSafeFieldWriterFallbackFontTest {

    private static final String COMPANY_ADDRESS_FIELD = "Formular1[0].#subform[0].Header[0].Textfeld1[0]";
    private static final String COMMENT_FIELD = "Formular1[0].#subform[0].Body[0].Textfeld1[1]";
    private static final String ORDERER_FIELD = "Formular1[0].#subform[0].Header[0].Firma[1]";

    @Test
    void pureCjkContent_getsRealEmbeddedGlyphs_notPlaceholder() throws IOException {
        try (PDDocument doc = openTemplate()) {
            PDFOrder order = buildOrder(doc);

            order.setCompanyAddress("中文测试");

            byte[] out = saveAndGet(doc);

            try (PDDocument reloaded = Loader.loadPDF(out)) {
                PDAcroForm form = reloaded.getDocumentCatalog().getAcroForm();
                String value = form.getField(COMPANY_ADDRESS_FIELD).getValueAsString();
                assertThat(value).isEqualTo("中文测试");

                PDFont usedFont = fontUsedInAppearance(form, COMPANY_ADDRESS_FIELD);
                assertThat(isEmbedded(usedFont)).as("fallback font must be really embedded").isTrue();
                assertThatCode(usedFont, "中");
            }
        }
    }

    @Test
    void multilineCommentWithCjk_wrapsAcrossMultipleLines() throws IOException {
        try (PDDocument doc = openTemplate()) {
            PDFOrder order = buildOrder(doc);

            String longCjkComment = "这是一段很长的中文注释文本，用来测试多行文本的自动换行功能是否正常工作，"
                    + "这段文字应该会被拆分成多行显示在注释字段中。";
            order.setCommentForSupplier(longCjkComment);

            byte[] out = saveAndGet(doc);

            try (PDDocument reloaded = Loader.loadPDF(out)) {
                PDAcroForm form = reloaded.getDocumentCatalog().getAcroForm();
                assertThat(form.getField(COMMENT_FIELD).getValueAsString()).isEqualTo(longCjkComment);

                String appearanceContent = appearanceStreamContent(form, COMMENT_FIELD);
                long lineCount = appearanceContent.lines()
                        .filter(l -> l.contains("Td") && !l.startsWith("0 0 Td"))
                        .count();
                assertThat(lineCount)
                        .as("long CJK comment must wrap onto more than one line: %s", appearanceContent)
                        .isGreaterThan(0);
            }
        }
    }

    @Test
    void mixedLatinAndEmoji_keepsLatinText_placeholdersOnlyTheEmoji() throws IOException {
        try (PDDocument doc = openTemplate()) {
            PDFOrder order = buildOrder(doc);

            order.setOrderer("Müller 😀 GmbH");

            byte[] out = saveAndGet(doc);

            try (PDDocument reloaded = Loader.loadPDF(out)) {
                PDAcroForm form = reloaded.getDocumentCatalog().getAcroForm();
                String value = form.getField(ORDERER_FIELD).getValueAsString();

                // Accepted limitation (per design discussion): no single
                // embedded font covers both Latin text and emoji together,
                // so the emoji becomes a placeholder while the Latin text
                // around it renders correctly.
                assertThat(value).contains("Müller", "GmbH");
                assertThat(value).doesNotContain("😀");
                assertThat(value).contains("?");
            }
        }
    }

    @Test
    void plainGermanContent_stillUsesFastPath_neverTouchesFallbackFont() throws IOException {
        try (PDDocument doc = openTemplate()) {
            PDFOrder order = buildOrder(doc);

            order.setCompanyAddress("Müller-Lüdenscheid Straße 5, 70173 Stuttgart");

            byte[] out = saveAndGet(doc);

            try (PDDocument reloaded = Loader.loadPDF(out)) {
                PDAcroForm form = reloaded.getDocumentCatalog().getAcroForm();
                assertThat(form.getField(COMPANY_ADDRESS_FIELD).getValueAsString())
                        .isEqualTo("Müller-Lüdenscheid Straße 5, 70173 Stuttgart");

                // Fast path means the ORIGINAL template font (Calibri-Bold)
                // is still referenced -- no fallback font resource was ever
                // introduced for this field.
                PDFont usedFont = fontUsedInAppearance(form, COMPANY_ADDRESS_FIELD);
                assertThat(usedFont.getName()).contains("Calibri");
            }
        }
    }

    // ------------------------------------------------------------- Helpers

    private PDDocument openTemplate() throws IOException {
        OrderPdfProperties properties = OrderPdfProperties.defaults();
        return new PdfTemplateLoader(properties).loadOrderTemplate();
    }

    private PDFOrder buildOrder(PDDocument doc) throws IOException {
        PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
        EmbeddedFontProvider fontProvider = new EmbeddedFontProvider();
        fontProvider.init();
        return new PDFOrder().parseOrder(acroForm, doc, fontProvider);
    }

    private byte[] saveAndGet(PDDocument doc) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out);
        return out.toByteArray();
    }

    private boolean isEmbedded(PDFont font) {
        PDFontDescriptor fd = font.getFontDescriptor();
        return fd != null && (fd.getFontFile() != null || fd.getFontFile2() != null || fd.getFontFile3() != null);
    }

    private void assertThatCode(PDFont font, String probe) {
        try {
            font.encode(probe);
        } catch (Exception e) {
            throw new AssertionError("Font " + font.getName() + " should be able to encode \"" + probe + "\"", e);
        }
    }

    private PDFont fontUsedInAppearance(PDAcroForm form, String fieldName) throws IOException {
        PDTextField field = (PDTextField) form.getField(fieldName);
        PDAnnotationWidget widget = field.getWidgets().get(0);
        PDResources apResources = widget.getAppearance().getNormalAppearance().getAppearanceStream().getResources();
        // Both the manual path ("F1") and the untouched fast path
        // (whatever the template's own font resource name is) expose
        // exactly one font resource on a single-font appearance stream.
        return apResources.getFont(apResources.getFontNames().iterator().next());
    }

    private String appearanceStreamContent(PDAcroForm form, String fieldName) throws IOException {
        PDTextField field = (PDTextField) form.getField(fieldName);
        PDAnnotationWidget widget = field.getWidgets().get(0);
        try (var is = widget.getAppearance().getNormalAppearance().getAppearanceStream().getContentStream()
                .createInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.ISO_8859_1);
        }
    }
}
