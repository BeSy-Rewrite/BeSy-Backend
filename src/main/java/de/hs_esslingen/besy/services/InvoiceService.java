package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.InvoiceRequestDTO;
import de.hs_esslingen.besy.dtos.response.InvoiceResponseDTO;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.request.InvoiceRequestMapper;
import de.hs_esslingen.besy.mappers.response.InvoiceResponseMapper;
import de.hs_esslingen.besy.models.Invoice;
import de.hs_esslingen.besy.repositories.InvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceResponseMapper invoiceResponseMapper;
    private final InvoiceRequestMapper invoiceRequestMapper;


    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices(Long orderId) {
        List<Invoice> invoices = invoiceRepository.findAllByOrderId(orderId);
        List<InvoiceResponseDTO> invoiceResponseDTOS = invoiceResponseMapper.toDto(invoices);
        return ResponseEntity.ok(invoiceResponseDTOS);
    }

    public ResponseEntity<InvoiceResponseDTO> createInvoice(InvoiceRequestDTO invoiceDTO, Long orderId) {
        Invoice invoice = invoiceRequestMapper.toEntity(invoiceDTO);
        invoice.setOrderId(orderId);
        invoice.setCreatedDate(OffsetDateTime.now());
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return ResponseEntity.ok(invoiceResponseMapper.toDto(savedInvoice));
    }

    public boolean existsInvoiceById(String id) {
        return invoiceRepository.existsById(id);
    }
}
