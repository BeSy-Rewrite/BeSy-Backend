package de.hs_esslingen.besy.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.springframework.stereotype.Component;

import de.hs_esslingen.besy.services.OrderService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderPdfGenerator {

    private final OrderPdfDataLoader dataLoader;
    private final OrderService orderService;
    private final OrderPdfProperties properties;
    private final PdfTemplateLoader templateLoader;
    private final OrderPdfFormWriter formWriter;
    private final EmbeddedFontProvider fontProvider;

    public byte[] generate(Long orderId) throws IOException {
        try (PDDocument document = templateLoader.loadOrderTemplate()) {
            PDFOrder form = bindForm(document);

            OrderPdfData data = dataLoader.load(orderId);
            String orderNumber = orderService.getOrderNumber(data.order()).orElse("");

            OrderPdfTotals totals = OrderPdfCalculator.calculate(
                    data.items(),
                    data.order().getPercentageDiscount(),
                    properties.getDefaultVatRate());

            formWriter.write(form, data, totals, orderNumber);

            return toBytes(document);
        }
    }

    private PDFOrder bindForm(PDDocument document) {
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        PDFOrder form = new PDFOrder();
        form.parseOrder(acroForm, document, fontProvider);
        acroForm.setXFA(null);
        return form;
    }

    private byte[] toBytes(PDDocument document) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        return baos.toByteArray();
    }
}
