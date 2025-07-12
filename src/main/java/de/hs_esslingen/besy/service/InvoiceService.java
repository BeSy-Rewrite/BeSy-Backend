package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.response.InvoiceResponseDTO;
import de.hs_esslingen.besy.exception.NotFoundException;
import de.hs_esslingen.besy.mapper.response.InvoiceResponseMapper;
import de.hs_esslingen.besy.model.Invoice;
import de.hs_esslingen.besy.repository.InvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceResponseMapper invoiceResponseMapper;

    public ResponseEntity<InvoiceResponseDTO> getInvoiceOfOrder(Long orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId).orElseThrow(() -> new NotFoundException("Invoice with order id " + orderId + " not found."));
        InvoiceResponseDTO invoiceResponseDTO = invoiceResponseMapper.toDto(invoice);
        return ResponseEntity.ok(invoiceResponseDTO);
    }
}
