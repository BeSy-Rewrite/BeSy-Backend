package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.InvoiceRequestDTO;
import de.hs_esslingen.besy.dtos.response.InvoiceResponseDTO;
import de.hs_esslingen.besy.mappers.request.InvoiceRequestMapper;
import de.hs_esslingen.besy.mappers.response.InvoiceResponseMapper;
import de.hs_esslingen.besy.models.Invoice;
import de.hs_esslingen.besy.repositories.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceResponseMapper invoiceResponseMapper;

    @Mock
    private InvoiceRequestMapper invoiceRequestMapper;

    @InjectMocks
    private InvoiceService invoiceService;

    private InvoiceRequestDTO requestDto;
    private Invoice invoice;
    private InvoiceResponseDTO responseDto;

    @BeforeEach
    void setUp() {
        requestDto = new InvoiceRequestDTO(
                "INV-1",
                "CC-1",
                100L,
                BigDecimal.valueOf(123.45),
                LocalDate.of(2025, 1, 10),
                "Comment",
                999L
        );
        invoice = new Invoice();
        invoice.setId("INV-1");
        invoice.setCostCenterId("CC-1");
        invoice.setPrice(BigDecimal.valueOf(123.45));
        invoice.setDate(LocalDate.of(2025, 1, 10));
        invoice.setComment("Comment");
        invoice.setPaperlessId(999L);
        responseDto = new InvoiceResponseDTO(
                "INV-1",
                "CC-1",
                100L,
                BigDecimal.valueOf(123.45),
                LocalDate.of(2025, 1, 10),
                "Comment",
                OffsetDateTime.now(),
                999L
        );
    }

    @Test
    void should_return_all_invoices_as_dtos() {
        Long orderId = 100L;
        List<Invoice> invoices = List.of(invoice);
        List<InvoiceResponseDTO> dtos = List.of(responseDto);

        when(invoiceRepository.findAllByOrderId(orderId)).thenReturn(invoices);
        when(invoiceResponseMapper.toDto(invoices)).thenReturn(dtos);

        ResponseEntity<List<InvoiceResponseDTO>> response = invoiceService.getAllInvoices(orderId);

        assertSame(dtos, response.getBody());
        verify(invoiceRepository).findAllByOrderId(orderId);
        verify(invoiceResponseMapper).toDto(invoices);
        verify(invoiceRepository, never()).delete(any(Invoice.class));
        verify(invoiceRepository, never()).deleteById(anyString());
        verify(invoiceRepository, never()).deleteAll();
    }

    @Test
    void should_create_invoice_set_fields_and_return_dto() {
        Long orderId = 100L;

        when(invoiceRequestMapper.toEntity(requestDto)).thenReturn(invoice);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceResponseMapper.toDto(any(Invoice.class))).thenReturn(responseDto);

        ResponseEntity<InvoiceResponseDTO> response = invoiceService.createInvoice(requestDto, orderId);

        assertSame(responseDto, response.getBody());

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        Invoice saved = captor.getValue();
        assertEquals(orderId, saved.getOrderId());
        assertNotNull(saved.getCreatedDate());

        verify(invoiceRequestMapper).toEntity(requestDto);
        verify(invoiceResponseMapper).toDto(saved);
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
