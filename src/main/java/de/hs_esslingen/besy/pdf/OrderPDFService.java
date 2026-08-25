package de.hs_esslingen.besy.pdf;

import java.io.IOException;

import org.springframework.stereotype.Service;

import de.hs_esslingen.besy.services.OrderService;
import lombok.RequiredArgsConstructor;

/**
 * Thin facade kept for the existing callers (see
 * {@code OrderController#exportOrder}). All orchestration lives in
 * {@link OrderPdfGenerator}.
 */
@Service
@RequiredArgsConstructor
public class OrderPDFService {

    private final OrderPdfGenerator generator;

    /**
     * Bridge for tests that wire the collaborators by hand without a Spring
     * context (OrderPdfGoldenTest). Remove in a later step once those tests
     * construct {@link OrderPdfGenerator} directly.
     */
    public OrderPDFService(
            OrderPdfDataLoader dataLoader,
            OrderService orderService,
            OrderPdfProperties properties,
            PdfTemplateLoader templateLoader,
            OrderPdfFormWriter formWriter) {
        this(new OrderPdfGenerator(dataLoader, orderService, properties, templateLoader, formWriter));
    }

    public byte[] generateOrderPDF(Long orderId) throws IOException {
        return generator.generate(orderId);
    }
}
