package de.hs_esslingen.besy.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.jupiter.api.Test;

class PdfSafeFieldWriterTest {

    @Test
    void neverThrowsAndReplacesUnsupportedCodePointsWithPlaceholder() throws IOException {
        OrderPdfProperties properties = OrderPdfProperties.defaults();
        PdfTemplateLoader loader = new PdfTemplateLoader(properties);

        try (PDDocument doc = loader.loadOrderTemplate()) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            EmbeddedFontProvider fontProvider = new EmbeddedFontProvider();
            fontProvider.init();
            PDFOrder order = new PDFOrder().parseOrder(acroForm, doc, fontProvider);

            // Must not throw despite emoji + CJK + Latin-1 mix.
            order.setCompanyAddress("Müller-Lüdenscheid Straße 😀 中文 é ñ ß");
            order.setCommentForSupplier("Comment with emoji 😀😀 and CJK 中文测试");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);

            try (PDDocument reloaded = Loader.loadPDF(out.toByteArray())) {
                PDAcroForm reloadedForm = reloaded.getDocumentCatalog().getAcroForm();

                String companyAddress = valueOf(reloadedForm,
                        "Formular1[0].#subform[0].Header[0].Textfeld1[0]");
                String comment = valueOf(reloadedForm,
                        "Formular1[0].#subform[0].Body[0].Textfeld1[1]");

                // Latin-1 range (umlauts, ß, é, ñ) is preserved as-is.
                assertThat(companyAddress).contains("Müller-Lüdenscheid Straße", "é", "ñ", "ß");

                // CJK/emoji are now REAL GLYPHS (fallback font), not placeholders --
                // see PdfSafeFieldWriterFallbackFontTest for the embedding proof.
                assertThat(companyAddress).contains("中", "文");
                assertThat(comment).contains("中", "文");
                assertThat(containsLoneSurrogate(companyAddress)).isFalse();
                assertThat(containsLoneSurrogate(comment)).isFalse();
            }
        }
    }

    @Test
    void leavesPureLatin1ValuesUnchanged() throws IOException {
        OrderPdfProperties properties = OrderPdfProperties.defaults();
        PdfTemplateLoader loader = new PdfTemplateLoader(properties);

        try (PDDocument doc = loader.loadOrderTemplate()) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            EmbeddedFontProvider fontProvider = new EmbeddedFontProvider();
            fontProvider.init();
            PDFOrder order = new PDFOrder().parseOrder(acroForm, doc, fontProvider);

            String value = "Büro Straße äöüÄÖÜß";
            order.setCompanyAddress(value);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);

            try (PDDocument reloaded = Loader.loadPDF(out.toByteArray())) {
                PDAcroForm reloadedForm = reloaded.getDocumentCatalog().getAcroForm();
                String actual = valueOf(reloadedForm, "Formular1[0].#subform[0].Header[0].Textfeld1[0]");
                assertThat(actual).isEqualTo(value);
            }
        }
    }

    private String valueOf(PDAcroForm form, String qualifiedName) {
        PDField field = form.getField(qualifiedName);
        return field == null ? null : field.getValueAsString();
    }

    private boolean containsLoneSurrogate(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isHighSurrogate(c)) {
                if (i + 1 >= s.length() || !Character.isLowSurrogate(s.charAt(i + 1))) {
                    return true;
                }
                i++;
            } else if (Character.isLowSurrogate(c)) {
                return true;
            }
        }
        return false;
    }
}
