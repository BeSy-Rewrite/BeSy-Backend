package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.InvoiceResponseDTO;
import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.response.InvoiceResponseMapper;
import de.hs_esslingen.besy.models.Invoice;
import de.hs_esslingen.besy.repositories.InvoiceRepository;
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

    public boolean existsInvoiceById(String id) {
        return invoiceRepository.existsById(id);
    }
}
