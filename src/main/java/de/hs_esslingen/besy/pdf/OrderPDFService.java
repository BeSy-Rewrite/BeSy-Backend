package de.hs_esslingen.besy.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.springframework.stereotype.Service;

import de.hs_esslingen.besy.services.OrderService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderPDFService {

    private final OrderPdfDataLoader dataLoader;
    private final OrderService orderService;
    private final OrderPdfProperties properties;
    private final PdfTemplateLoader templateLoader;
    private final OrderPdfFormWriter formWriter;

    // TODO: Ensure that this method is only called when the Order is in a state
    // where all necessary constraints and relationships are satisfied
    public byte[] generateOrderPDF(Long orderId) throws IOException {
        try (PDDocument document = templateLoader.loadOrderTemplate()) {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            PDFOrder order = new PDFOrder();
            order.parseOrder(acroForm);
            acroForm.setXFA(null);

            OrderPdfData data = dataLoader.load(orderId);
            String orderNumber = orderService.getOrderNumber(data.order()).orElse("");

            OrderPdfTotals totals = OrderPdfCalculator.calculate(
                    data.items(),
                    data.order().getPercentageDiscount(),
                    properties.getDefaultVatRate());

            formWriter.write(order, data, totals, orderNumber);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
}
