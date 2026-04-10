package de.hs_esslingen.besy.extern.bic;

import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.models.Invoice;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.services.InvoiceService;
import de.hs_esslingen.besy.services.OrderService;
import de.hs_esslingen.besy.services.PaperlessService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@EnableRetry
@AllArgsConstructor
public class BicCallbackService {

    private static final Logger logger = LoggerFactory.getLogger(BicCallbackService.class);


    private final OrderService orderService;
    private final PaperlessService paperlessService;
    private final InvoiceService invoiceService;

    public void handleCallback(String orderNumber, String status, Jwt jwt) {
        Order order = orderService.getOrderByOrderNumber(orderNumber.replace("-", "/"));
        OrderStatus newStatus = status.equalsIgnoreCase("successful") ? OrderStatus.SETTLED : OrderStatus.REJECTED;
        logger.info("Order {} status changed to {}", orderNumber, newStatus);
        orderService.updateOrderStatus(order.getId(), newStatus, jwt);
    }

    public void handleFileCallback(String orderNumber, MultipartFile file) {
        Order order = orderService.getOrderByOrderNumber(orderNumber.replace("-", "/"));

        Invoice invoice = new Invoice();
        invoice.setComment("BIC Report");
        invoice.setDate(OffsetDateTime.now().toLocalDate());
        invoice.setCostCenterId(order.getPrimaryCostCenterId());
        invoice.setOrderId(order.getId());
        invoice.setPrice(BigDecimal.valueOf(0.0));
        invoice.setId(OffsetDateTime.now().toString());

        Invoice newInvoice = invoiceService.createInvoice(invoice, order.getId());

        try {
            paperlessService.uploadPdfToPaperless(file, newInvoice.getId());
        } catch (Exception e) {
            logger.error("Failed to upload PDF to paperless service for order {}, invoice {}", orderNumber, newInvoice.getId(), e);
            throw new RuntimeException(e);
        }
    }
}
