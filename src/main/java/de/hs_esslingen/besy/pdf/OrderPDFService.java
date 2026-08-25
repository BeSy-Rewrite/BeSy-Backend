package de.hs_esslingen.besy.pdf;

import java.io.IOException;

import org.springframework.stereotype.Service;

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

    public byte[] generateOrderPDF(Long orderId) throws IOException {
        return generator.generate(orderId);
    }
}
