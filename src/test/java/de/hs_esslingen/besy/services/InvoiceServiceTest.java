package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.models.Invoice;
import de.hs_esslingen.besy.repositories.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
        invoice.setId("INV-1");
        invoice.setCostCenterId("CC-1");
        invoice.setPrice(BigDecimal.valueOf(123.45));
        invoice.setDate(LocalDate.of(2025, 1, 10));
        invoice.setComment("Comment");
        invoice.setPaperlessId(999L);
    }

    @Test
    void should_return_all_invoices() {
        Long orderId = 100L;
        List<Invoice> invoices = List.of(invoice);

        when(invoiceRepository.findAllByOrderId(orderId)).thenReturn(invoices);

        List<Invoice> response = invoiceService.getAllInvoices(orderId);

        assertSame(invoices, response);
        verify(invoiceRepository).findAllByOrderId(orderId);
        verify(invoiceRepository, never()).delete(any(Invoice.class));
        verify(invoiceRepository, never()).deleteById(anyString());
        verify(invoiceRepository, never()).deleteAll();
    }


    @Test
    void should_return_true_when_invoice_exists() {
        when(invoiceRepository.existsById("INV-2")).thenReturn(true);

        boolean result = invoiceService.existsInvoiceById("INV-2");

        assertEquals(true, result);
        verify(invoiceRepository).existsById("INV-2");
        verify(invoiceRepository, never()).delete(any(Invoice.class));
        verify(invoiceRepository, never()).deleteById(anyString());
        verify(invoiceRepository, never()).deleteAll();
    }

    @Test
    void should_return_false_when_invoice_does_not_exist() {
        when(invoiceRepository.existsById("INV-3")).thenReturn(false);

        boolean result = invoiceService.existsInvoiceById("INV-3");

        assertEquals(false, result);
        verify(invoiceRepository).existsById("INV-3");
        verify(invoiceRepository, never()).delete(any(Invoice.class));
        verify(invoiceRepository, never()).deleteById(anyString());
        verify(invoiceRepository, never()).deleteAll();
    }
}
