package de.hs_esslingen.besy.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.springframework.stereotype.Component;

import de.hs_esslingen.besy.services.OrderService;
import lombok.RequiredArgsConstructor;

/**
 * Orchestrates the order PDF generation: template -> AcroForm binding ->
 * data -> totals -> form writer -> bytes.
 *
 * <p>
 * The step order is intentionally identical to the previous inline code in
 * {@link OrderPDFService} (document first, then data), so that both the
 * produced field values and the order in which failures surface stay
 * unchanged.
 *
 * <p>
 * The document is owned and closed here; {@link OrderPdfFormWriter} never sees
 * a {@link PDDocument}.
 */
@Component
@RequiredArgsConstructor
public class OrderPdfGenerator {

    private final OrderPdfDataLoader dataLoader;
    private final OrderService orderService;
    private final OrderPdfProperties properties;
    private final PdfTemplateLoader templateLoader;
    private final OrderPdfFormWriter formWriter;

    // TODO: Ensure that this method is only called when the Order is in a state
    // where all necessary constraints and relationships are satisfied
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

    /** Parses the AcroForm into the typed binding and drops the XFA layer. */
    private PDFOrder bindForm(PDDocument document) throws IOException {
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        PDFOrder form = new PDFOrder();
        form.parseOrder(acroForm);
        acroForm.setXFA(null);
        return form;
    }

    private byte[] toBytes(PDDocument document) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        return baos.toByteArray();
    }
}
