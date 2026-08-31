package de.hs_esslingen.besy.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText;
import org.junit.jupiter.api.Test;

import de.hs_esslingen.besy.enums.VatType;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.ItemId;
import de.hs_esslingen.besy.models.Vat;

/**
 * THROWAWAY / READ-ONLY DIAGNOSTIC — not part of the permanent suite.
 *
 * Reproduces the Unicode/wrapping issues described in the session goal and
 * inspects the template's embedded fonts. Prints findings to stdout for
 * manual review; does not modify production code.
 *
 * Run in isolation:
 * mvn -q test-compile && mvn test -Dtest=UnicodeDiagnostic
 */
class UnicodeDiagnostic {

    private static final Map<String, String> PROBES = new LinkedHashMap<>();
    static {
        PROBES.put("umlaut ä (U+00E4)", "ä");
        PROBES.put("umlaut ü (U+00FC)", "ü");
        PROBES.put("eszett ß (U+00DF)", "ß");
        PROBES.put("accent é (U+00E9)", "é");
        PROBES.put("CJK 中文", "中文");
        PROBES.put("emoji 😀 (surrogate pair)", "😀");
    }

    // ------------------------------------------------------------- 1) Font
    // inventory

    @Test
    void listAllFontsInAcroFormDR() throws IOException {
        OrderPdfProperties props = OrderPdfProperties.defaults();
        PdfTemplateLoader loader = new PdfTemplateLoader(props);
        try (PDDocument doc = loader.loadOrderTemplate()) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            PDResources dr = acroForm.getDefaultResources();

            System.out.println("=== Fonts in AcroForm Default Resources (DR) ===");
            assertThat(dr).as("AcroForm must have Default Resources").isNotNull();

            for (COSName fontName : dr.getFontNames()) {
                PDFont font = dr.getFont(fontName);
                System.out.println("--- Resource name: /" + fontName.getName() + " ---");
                describeFont(font);
                probeGlyphCoverage(font);
            }
        }
    }

    @Test
    void describeFontsForKeyWidgets() throws IOException {
        OrderPdfProperties props = OrderPdfProperties.defaults();
        PdfTemplateLoader loader = new PdfTemplateLoader(props);
        try (PDDocument doc = loader.loadOrderTemplate()) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();

            // Widgets we actually write into (per PDFOrder.parseOrder mapping)
            describeFieldFont(acroForm, "Formular1[0].#subform[0].Header[0].Textfeld1[0]"); // companyAddress
            describeFieldFont(acroForm, "Formular1[0].#subform[0].Body[0].Textfeld1[1]"); // commentForSupplier
            describeFieldFont(acroForm, "Formular1[0].#subform[0].Header[0].Firma[1]"); // orderer
            describeFieldFont(acroForm, "Formular1[0].#subform[0].Header[0].Telefon[3]"); // deliveryOrderer
            describeFieldFont(acroForm, "Formular1[0].#subform[0].Body[0].Beschreibung[0]"); // item description
                                                                                             // (Helvetica!)
            describeFieldFont(acroForm, "Formular1[0].#subform[1].Textfeld5[0]"); // otherReasonsDescription
            describeFieldFont(acroForm, "Formular1[0].#subform[1].Textfeld7[0]"); // quotation company name
        }
    }

    private void describeFieldFont(PDAcroForm acroForm, String qualifiedName) throws IOException {
        System.out.println("=== Field: " + qualifiedName + " ===");
        PDField field = acroForm.getField(qualifiedName);
        if (field == null) {
            System.out.println("  (field not found)");
            return;
        }
        if (!(field instanceof PDVariableText variableTextField)) {
            System.out.println("  (field is not a PDVariableText, no DA string) - actual type: "
                    + field.getClass().getSimpleName());
            return;
        }

        String da = variableTextField.getDefaultAppearance();
        if (da == null || da.isBlank()) {
            da = acroForm.getDefaultAppearance();
            System.out.println("  DA (inherited from AcroForm): " + da);
        } else {
            System.out.println("  DA (field-level): " + da);
        }
        String fontResourceName = extractFontName(da);
        System.out.println("  Resolved font resource name: " + fontResourceName);
        if (fontResourceName == null) {
            System.out.println("  Could not parse font name from DA string.");
            return;
        }
        PDResources dr = acroForm.getDefaultResources();
        PDFont font = dr == null ? null : dr.getFont(COSName.getPDFName(fontResourceName));
        if (font == null) {
            System.out.println("  Font resource not found in AcroForm DR.");
            return;
        }
        describeFont(font);
        probeGlyphCoverage(font);
    }

    private String extractFontName(String da) {
        if (da == null) {
            return null;
        }
        Matcher m = Pattern.compile("/(\\S+)\\s+[-\\d.]+\\s+Tf").matcher(da);
        return m.find() ? m.group(1) : null;
    }

    private void describeFont(PDFont font) {
        System.out.println("  BaseFont: " + font.getName());
        System.out.println("  Subtype: " + font.getCOSObject().getNameAsString(COSName.SUBTYPE));
        PDFontDescriptor fd = font.getFontDescriptor();
        boolean embedded = fd != null
                && (fd.getFontFile() != null || fd.getFontFile2() != null || fd.getFontFile3() != null);
        System.out.println("  FontDescriptor present: " + (fd != null));
        System.out.println("  Embedded (FontFile/2/3): " + embedded);
        Object enc = font.getCOSObject().getDictionaryObject(COSName.ENCODING);
        System.out.println("  Encoding entry: " + enc);
        Object toUnicode = font.getCOSObject().getDictionaryObject(COSName.TO_UNICODE);
        System.out.println("  ToUnicode present: " + (toUnicode != null));
        System.out.println("  Is Standard-14 non-embedded: " + (font instanceof PDType1Font && !embedded));
    }

    private void probeGlyphCoverage(PDFont font) {
        for (Map.Entry<String, String> e : PROBES.entrySet()) {
            try {
                font.encode(e.getValue());
                float width = font.getStringWidth(e.getValue());
                System.out.println("    [OK]   " + e.getKey() + " -> encode() succeeded, width=" + width);
            } catch (Exception ex) {
                System.out.println("    [FAIL] " + e.getKey() + " -> "
                        + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            }
        }
    }

    // ------------------------------------------------------------- 2) Direct width
    // measurement

    @Test
    void directWidthMeasurementRevealsStrippingBug() throws Exception {
        OrderPdfProperties props = OrderPdfProperties.defaults();
        PdfTemplateLoader loader = new PdfTemplateLoader(props);
        try (PDDocument doc = loader.loadOrderTemplate()) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            EmbeddedFontProvider fontProvider = new EmbeddedFontProvider();
            fontProvider.init();
            PDFOrder order = new PDFOrder().parseOrder(acroForm, doc, fontProvider);

            Method getStringWidth = PDFOrder.class.getDeclaredMethod("getStringWidth", String.class);
            getStringWidth.setAccessible(true);

            String[] probes = { "Buro", "Büro", "Strasse", "Straße", "中文测试", "😀", "😀😀😀" };
            System.out.println("=== getStringWidth() direct measurement ===");
            for (String p : probes) {
                float width = (float) getStringWidth.invoke(order, p);
                System.out.printf("  getStringWidth(\"%s\") = %.3f%n", p, width);
            }
        }
    }

    // ------------------------------------------------------------- 3) End-to-end
    // wrap repro

    @Test
    void wrapReproUmlautAndEmojiCjk() throws IOException {
        OrderPdfProperties props = OrderPdfProperties.defaults();
        PdfTemplateLoader loader = new PdfTemplateLoader(props);
        try (PDDocument doc = loader.loadOrderTemplate()) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            EmbeddedFontProvider fontProvider = new EmbeddedFontProvider();
            fontProvider.init();
            PDFOrder order = new PDFOrder().parseOrder(acroForm, doc, fontProvider);

            Item umlautItem = buildItem(1, 1,
                    "Bürostuhl mit höhenverstellbarer Rückenlehne für Ergänzungsmöbel äöüÄÖÜß Testtexttesttext");
            Item emojiCjkItem = buildItem(1, 2,
                    "Wichtiger Artikel 😀😀😀😀😀 mit chinesischen Schriftzeichen 中文测试字符串 "
                            + "und weiterem Text der zum Zeilenumbruch führen sollte");

            order.setItems(List.of(umlautItem, emojiCjkItem));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);

            try (PDDocument reloaded = Loader.loadPDF(out.toByteArray())) {
                PDAcroForm reloadedForm = reloaded.getDocumentCatalog().getAcroForm();
                System.out.println("=== Beschreibung[i] after wrapping (umlaut + emoji/CJK items) ===");
                for (int i = 0; i < PDFOrder.AMOUNT_ITEM_LINES; i++) {
                    PDField f = reloadedForm.getField(
                            String.format("Formular1[0].#subform[0].Body[0].Beschreibung[%d]", i));
                    String value = f == null ? null : f.getValueAsString();
                    if (value != null && !value.isBlank()) {
                        System.out.printf("  [%2d] len=%3d loneSurrogate=%-5s value=%s%n",
                                i, value.length(), containsLoneSurrogate(value), value);
                    }
                }
            }
        }
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

    private Item buildItem(long orderId, int itemId, String name) {
        Vat vat = new Vat();
        vat.setValue(new BigDecimal("19.00"));
        vat.setDescription("VAT 19.00");

        Item item = new Item();
        item.setId(new ItemId(orderId, itemId));
        item.setName(name);
        item.setPricePerUnit(new BigDecimal("10.00"));
        item.setQuantity(1L);
        item.setVat(vat);
        item.setVatValue(vat.getValue());
        item.setVatType(VatType.netto);
        item.setMigratedToInsy(false);
        return item;
    }

    // ------------------------------------------------------------- 4) AcroForm
    // value round-trip

    @Test
    void acroFormValueRoundTripForCalibriFields() throws IOException {
        OrderPdfProperties props = OrderPdfProperties.defaults();
        PdfTemplateLoader loader = new PdfTemplateLoader(props);
        try (PDDocument doc = loader.loadOrderTemplate()) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            EmbeddedFontProvider fontProvider = new EmbeddedFontProvider();
            fontProvider.init();
            PDFOrder order = new PDFOrder().parseOrder(acroForm, doc, fontProvider);

            String probe = "Müller-Lüdenscheid Straße 😀 中文 é ñ ß";

            System.out.println("=== Writing probe string into Calibri-backed fields ===");
            trySet("companyAddress", () -> order.setCompanyAddress(probe));
            trySet("commentForSupplier", () -> order.setCommentForSupplier(probe));
            trySet("orderer", () -> order.setOrderer(probe));
            trySet("deliveryOrderer", () -> order.setDeliveryOrderer(probe));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);

            try (PDDocument reloaded = Loader.loadPDF(out.toByteArray())) {
                PDAcroForm reloadedForm = reloaded.getDocumentCatalog().getAcroForm();
                checkRoundTrip(reloadedForm, "Formular1[0].#subform[0].Header[0].Textfeld1[0]", probe);
                checkRoundTrip(reloadedForm, "Formular1[0].#subform[0].Body[0].Textfeld1[1]", probe);
                checkRoundTrip(reloadedForm, "Formular1[0].#subform[0].Header[0].Firma[1]", probe);
                checkRoundTrip(reloadedForm, "Formular1[0].#subform[0].Header[0].Telefon[3]", probe);
            }
        }
    }

    private interface ThrowingRunnable {
        void run() throws IOException;
    }

    private void trySet(String label, ThrowingRunnable r) {
        try {
            r.run();
            System.out.println("  [" + label + "] setter succeeded");
        } catch (Exception e) {
            System.out.println("  [" + label + "] setter FAILED: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void checkRoundTrip(PDAcroForm form, String qualifiedName, String expected) {
        PDField f = form.getField(qualifiedName);
        String actual = f == null ? null : f.getValueAsString();
        System.out.println("=== Round-trip: " + qualifiedName + " ===");
        System.out.println("  expected: " + expected);
        System.out.println("  actual:   " + actual);
        System.out.println("  matches:  " + expected.equals(actual));
    }
}
