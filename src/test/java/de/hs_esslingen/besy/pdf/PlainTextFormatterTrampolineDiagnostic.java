package de.hs_esslingen.besy.pdf;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.pdfbox.pdmodel.interactive.form.PlainTextFormatterTrampoline;
import org.junit.jupiter.api.Test;

class PlainTextFormatterTrampolineDiagnostic {

    private static final Path FONT_DIR = Path.of("src", "test", "resources", "fonts");
    private static final Path NOTO_SANS_SC = FONT_DIR.resolve("NotoSansSC-Regular.ttf");
    private static final Path NOTO_SANS = FONT_DIR.resolve("NotoSans-Regular.ttf");
    private static final String SINGLE_LINE_FIELD = "Formular1[0].#subform[0].Header[0].Textfeld1[0]";
    private static final String MULTI_LINE_FIELD = "Formular1[0].#subform[0].Body[0].Textfeld1[1]"; // commentForSupplier

    @Test
    void plainTextFormatter_withOurFontInstance_rendersRealCjkGlyphs_singleLine() throws IOException {
        assumeTrue(Files.exists(NOTO_SANS_SC), "Skipped: NotoSansSC-Regular.ttf not present.");

        try (PDDocument doc = openTemplate()) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            PDResources dr = acroForm.getDefaultResources();
            PDFont font = PDType0Font.load(doc, new FileInputStream(NOTO_SANS_SC.toFile()), true);
            dr.put(COSName.getPDFName("NotoSansSCPtfDiag"), font);

            PDTextField field = (PDTextField) acroForm.getField(SINGLE_LINE_FIELD);
            field.setDefaultAppearance("/NotoSansSCPtfDiag 10 Tf 0 g");
            field.getCOSObject().setString(COSName.V, "中文测");

            List<PDAnnotationWidget> widgets = field.getWidgets();
            PDAnnotationWidget widget = widgets.get(0);
            PDRectangle rect = widget.getRectangle();

            PDAppearanceStream appearance = new PDAppearanceStream(doc);
            PDResources apResources = new PDResources();
            apResources.put(COSName.getPDFName("F1"), font);
            appearance.setResources(apResources);
            appearance.setBBox(new PDRectangle(rect.getWidth(), rect.getHeight()));

            try (PDAppearanceContentStream cs = new PDAppearanceContentStream(appearance)) {
                PlainTextFormatterTrampoline.formatIntoAppearanceStream(
                        cs, font, 10f, "中文测", false, rect.getWidth() - 4);
            }

            setNormalAppearance(widget, appearance);

            // PDAppearanceContentStream never registers the font with the PDDocument
            // (it passes `null` as the document to its superclass internally), so the
            // normal "subset automatically at save() time" mechanism never fires for
            // fonts used only through this stream type. Do it ourselves.
            if (font instanceof PDType0Font type0Font) {
                type0Font.subset();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);

            verifyRealEmbeddingAndRendering(out.toByteArray(), "NotoSansSCPtfDiag", SINGLE_LINE_FIELD, "中");
        }
    }

    @Test
    void plainTextFormatter_withWrapping_producesRealMultiLineLayout() throws IOException {
        assumeTrue(Files.exists(NOTO_SANS), "Skipped: NotoSans-Regular.ttf not present.");

        try (PDDocument doc = openTemplate()) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            PDResources dr = acroForm.getDefaultResources();
            PDFont font = PDType0Font.load(doc, new FileInputStream(NOTO_SANS.toFile()), true);
            dr.put(COSName.getPDFName("NotoSansPtfDiag"), font);

            PDTextField field = (PDTextField) acroForm.getField(MULTI_LINE_FIELD);
            field.setDefaultAppearance("/NotoSansPtfDiag 10 Tf 0 g");
            String longText = "Dies ist ein sehr langer Kommentar mit Umlauten (äöüß), der garantiert "
                    + "über mehrere Zeilen umgebrochen werden muss, um in dieses Kommentarfeld zu passen.";
            field.getCOSObject().setString(COSName.V, longText);

            PDAnnotationWidget widget = field.getWidgets().get(0);
            PDRectangle rect = widget.getRectangle();

            PDAppearanceStream appearance = new PDAppearanceStream(doc);
            PDResources apResources = new PDResources();
            apResources.put(COSName.getPDFName("F1"), font);
            appearance.setResources(apResources);
            appearance.setBBox(new PDRectangle(rect.getWidth(), rect.getHeight()));

            try (PDAppearanceContentStream cs = new PDAppearanceContentStream(appearance)) {
                // wrapLines=true, multiLine=true -- this is the piece we want
                // PDFBox to do for us instead of writing our own line-breaker.
                PlainTextFormatterTrampoline.formatIntoAppearanceStream(
                        cs, font, 10f, longText, true, rect.getWidth() - 4);
            }

            setNormalAppearance(widget, appearance);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);

            System.out.println("=== Multi-line wrapped output via PlainTextFormatter, size="
                    + out.size() / 1024.0 + " KB ===");
            printAppearanceStreamContent(out.toByteArray(), MULTI_LINE_FIELD);
        }
    }

    private PDAnnotationWidget setNormalAppearance(PDAnnotationWidget widget, PDAppearanceStream appearance) {
        PDAppearanceDictionary appearanceDict = widget.getAppearance();
        if (appearanceDict == null) {
            appearanceDict = new PDAppearanceDictionary();
            widget.setAppearance(appearanceDict);
        }
        appearanceDict.setNormalAppearance(appearance);
        widget.setAppearance(appearanceDict);
        return widget;
    }

    private void verifyRealEmbeddingAndRendering(byte[] pdfBytes, String fontResourceName, String fieldName,
            String probeChar) throws IOException {
        try (PDDocument reloaded = Loader.loadPDF(pdfBytes)) {
            PDAcroForm acroForm = reloaded.getDocumentCatalog().getAcroForm();
            PDResources dr = acroForm.getDefaultResources();
            PDFont reloadedFont = dr.getFont(COSName.getPDFName(fontResourceName));

            PDFontDescriptor fd = reloadedFont.getFontDescriptor();
            boolean embedded = fd != null
                    && (fd.getFontFile() != null || fd.getFontFile2() != null || fd.getFontFile3() != null);
            System.out.println("=== PlainTextFormatter (trampoline) approach: embedding check ===");
            System.out.println("  Embedded=" + embedded);

            try {
                reloadedFont.encode(probeChar);
                System.out
                        .println("  reloadedFont.encode(\"" + probeChar + "\") SUCCEEDED -- real glyph data present.");
            } catch (Exception e) {
                System.out.println("  reloadedFont.encode(\"" + probeChar + "\") FAILED: " + e);
            }

            printAppearanceStreamContent(pdfBytes, fieldName);
        }
    }

    private void printAppearanceStreamContent(byte[] pdfBytes, String fieldName) throws IOException {
        try (PDDocument reloaded = Loader.loadPDF(pdfBytes)) {
            var field = (PDTextField) reloaded.getDocumentCatalog().getAcroForm().getField(fieldName);
            var widget = field.getWidgets().get(0);
            var appearanceStream = widget.getAppearance().getNormalAppearance().getAppearanceStream();
            System.out.println("  Appearance stream content:");
            try (var is = appearanceStream.getContentStream().createInputStream()) {
                System.out.println(new String(is.readAllBytes(), StandardCharsets.ISO_8859_1));
            }
        }
    }

    private PDDocument openTemplate() throws IOException {
        OrderPdfProperties properties = OrderPdfProperties.defaults();
        return new PdfTemplateLoader(properties).loadOrderTemplate();
    }
}
