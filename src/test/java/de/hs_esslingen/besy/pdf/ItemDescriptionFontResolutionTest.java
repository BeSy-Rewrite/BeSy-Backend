package de.hs_esslingen.besy.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.jupiter.api.Test;

/**
 * Confirms the wrap-measurement font now matches the template's actual
 * Beschreibung[0] DA font (Calibri), instead of the previously hardcoded
 * Helvetica.
 */
class ItemDescriptionFontResolutionTest {

    @Test
    void resolvesTheTemplatesActualFont_notHardcodedHelvetica() throws Exception {
        OrderPdfProperties properties = OrderPdfProperties.defaults();
        PdfTemplateLoader loader = new PdfTemplateLoader(properties);

        try (PDDocument doc = loader.loadOrderTemplate()) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            EmbeddedFontProvider fontProvider = new EmbeddedFontProvider();
            fontProvider.init();
            PDFOrder order = new PDFOrder().parseOrder(acroForm, doc, fontProvider);

            PDFont resolvedFont = itemDescriptionFontOf(order);

            assertThat(resolvedFont.getName()).contains("Calibri");
            assertThat(resolvedFont.getName()).doesNotContain("Helvetica");
        }
    }

    @Test
    void measurementNowMatchesCalibriMetrics_notHelvetica() throws Exception {
        OrderPdfProperties properties = OrderPdfProperties.defaults();
        PdfTemplateLoader loader = new PdfTemplateLoader(properties);

        try (PDDocument doc = loader.loadOrderTemplate()) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            EmbeddedFontProvider fontProvider = new EmbeddedFontProvider();
            fontProvider.init();
            PDFOrder order = new PDFOrder().parseOrder(acroForm, doc, fontProvider);

            java.lang.reflect.Method getStringWidth = PDFOrder.class
                    .getDeclaredMethod("getStringWidth", String.class);
            getStringWidth.setAccessible(true);

            float measured = (float) getStringWidth.invoke(order, "ä");

            // Helvetica's "ä" at 12pt would measure 556/1000 * 12 = 6.672.
            // Calibri's "ä" at 12pt measures 479/1000 * 12 = 5.748.
            // Assert we're clearly on the Calibri side, not the old Helvetica value.
            assertThat(measured).isLessThan(6.0f);
        }
    }

    private PDFont itemDescriptionFontOf(PDFOrder order) throws NoSuchFieldException, IllegalAccessException {
        Field field = PDFOrder.class.getDeclaredField("itemDescriptionFont");
        field.setAccessible(true);
        return (PDFont) field.get(order);
    }
}
