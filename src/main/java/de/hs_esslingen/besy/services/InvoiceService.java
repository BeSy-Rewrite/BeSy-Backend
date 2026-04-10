package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.models.Invoice;
import de.hs_esslingen.besy.repositories.InvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public List<Invoice> getAllInvoices(Long orderId) {
        return invoiceRepository.findAllByOrderId(orderId);
    }

    public Invoice createInvoice(Invoice invoice, Long orderId) {
        invoice.setOrderId(orderId);
        //TODO: change to value from request
        invoice.setCreatedDate(OffsetDateTime.now());
        return invoiceRepository.save(invoice);
    }

    public boolean existsInvoiceById(String id) {
        return invoiceRepository.existsById(id);
    }
}
