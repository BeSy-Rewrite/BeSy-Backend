package de.hs_esslingen.besy.pdf;

import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.jupiter.api.Test;

/**
 * READ-ONLY diagnostic for Nettosumme[0]/MwSt[0]/Rabatt[0] and neighbors.
 * Does not touch production code. Run with:
 * mvn test -Dtest=FieldGeometryDiagnostic
 * and paste the console output.
 */
@org.junit.jupiter.api.Disabled("Diagnostic helper; run explicitly via -Dtest=FieldGeometryDiagnostic")
class FieldGeometryDiagnostic {

    private static final String[] FULLY_QUALIFIED_NAMES = {
            "Formular1[0].#subform[0].Body[0].Nettosumme[0]",
            "Formular1[0].#subform[0].Body[0].Nettosumme[1]",
            "Formular1[0].#subform[0].Body[0].MwSt[0]",
            "Formular1[0].#subform[0].Body[0].MwStSatz[0]",
            "Formular1[0].#subform[0].Body[0].Rabatt[0]",
            "Formular1[0].#subform[0].Body[0].RabattText[0]",
            "Formular1[0].#subform[0].Body[0].Zwischensumme[0]",
            "Formular1[0].#subform[0].Body[0].Gesamtsumme[0]",
    };

    @Test
    void dumpFieldGeometryAndFlags() throws Exception {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("static/Bestellformular_V01_empty.pdf");
                PDDocument doc = Loader.loadPDF(in.readAllBytes())) {

            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();

            for (String fqn : FULLY_QUALIFIED_NAMES) {
                PDField field = form.getField(fqn);
                System.out.println("=================================================");
                System.out.println("FQN: " + fqn);
                if (field == null) {
                    System.out.println("  -> NOT FOUND");
                    continue;
                }
                System.out.println("  class          : " + field.getClass().getSimpleName());
                System.out.println("  fieldType      : " + field.getFieldType());
                System.out.println("  isReadOnly     : " + field.isReadOnly());
                System.out.println("  isRequired     : " + field.isRequired());
                System.out.println("  isNoExport     : " + field.isNoExport());
                System.out.println("  /TU tooltip    : " + field.getAlternateFieldName());
                System.out.println("  mapping name   : " + field.getMappingName());
                // System.out.println(" default value : " + field.getDefaultValue()); //
                // getDefaultValue() doesnt exist for PDTextField
                System.out.println("  current value  : " + field.getValueAsString());
                try {
                    System.out.println("  default appear.: "
                            + field.getCOSObject().getDictionaryObject(org.apache.pdfbox.cos.COSName.DA));
                } catch (Exception e) {
                    System.out.println("  default appear.: <error: " + e.getMessage() + ">");
                }

                List<PDAnnotationWidget> widgets = field.getWidgets();
                System.out.println("  widget count   : " + widgets.size());
                for (int i = 0; i < widgets.size(); i++) {
                    PDAnnotationWidget w = widgets.get(i);
                    System.out.println("  widget[" + i + "].rect     : " + w.getRectangle());
                    System.out.println("  widget[" + i + "].flags    : " + w.getAnnotationFlags());
                    Integer pageIndex = findPageIndex(doc, w);
                    System.out.println("  widget[" + i + "].page idx : " + pageIndex);
                }
            }
        }
    }

    private Integer findPageIndex(PDDocument doc, PDAnnotationWidget widget) {
        int idx = 0;
        for (PDPage page : doc.getPages()) {
            try {
                if (page.getAnnotations().stream().anyMatch(a -> a.getCOSObject().equals(widget.getCOSObject()))) {
                    return idx;
                }
            } catch (Exception ignored) {
                // ignore per-page annotation read errors
            }
            idx++;
        }
        return null;
    }
}
