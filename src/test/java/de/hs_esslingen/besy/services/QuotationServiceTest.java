package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.QuotationRequestDTO;
import de.hs_esslingen.besy.dtos.response.QuotationResponseDTO;
import de.hs_esslingen.besy.mappers.request.QuotationRequestMapper;
import de.hs_esslingen.besy.mappers.response.QuotationResponseMapper;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Quotation;
import de.hs_esslingen.besy.models.QuotationId;
import de.hs_esslingen.besy.repositories.OrderRepository;
import de.hs_esslingen.besy.repositories.QuotationRepository;
import de.hs_esslingen.besy.repositories.SupplierRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuotationServiceTest {

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private QuotationRequestMapper quotationRequestMapper;

    @Mock
    private QuotationResponseMapper quotationResponseMapper;

    @InjectMocks
    private QuotationService quotationService;

    private Order order;
    private Quotation quotation;
    private QuotationRequestDTO requestDto;
    private QuotationResponseDTO responseDto;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(100L);

        quotation = new Quotation();
        quotation.setCompanyName("Quote Co");
        quotation.setCompanyCity("Quote City");
        quotation.setPrice(BigDecimal.valueOf(123.45));
        quotation.setQuoteDate(LocalDate.of(2025, 1, 10));

        requestDto = new QuotationRequestDTO(
                null,
                LocalDate.of(2025, 1, 10),
                BigDecimal.valueOf(123.45),
                "Quote Co",
                "Quote City"
        );

        responseDto = new QuotationResponseDTO(
                (short) 1,
                LocalDate.of(2025, 1, 10),
                BigDecimal.valueOf(123.45),
                "Quote Co",
                "Quote City"
        );
    }

    @Test
    void should_get_quotations_of_order() {
        Long orderId = 100L;
        List<Quotation> quotations = List.of(quotation);
        List<QuotationResponseDTO> dtos = List.of(responseDto);

        when(quotationRepository.findByOrderId(orderId)).thenReturn(quotations);
        when(quotationResponseMapper.toDto(quotations)).thenReturn(dtos);

        ResponseEntity<List<QuotationResponseDTO>> response = quotationService.getQuotationsOfOrder(orderId);

        assertEquals(200, response.getStatusCode().value());
        assertSame(dtos, response.getBody());
        verify(quotationRepository).findByOrderId(orderId);
        verify(quotationResponseMapper).toDto(quotations);
    }

    @Test
    void should_create_quotations_with_server_side_ids_and_order_reference() {
        Long orderId = 100L;
        List<QuotationRequestDTO> requestDtos = List.of(requestDto, requestDto);

        Quotation quotation2 = new Quotation();
        quotation2.setCompanyName("Quote Co 2");
        quotation2.setCompanyCity("Quote City 2");
        quotation2.setPrice(BigDecimal.valueOf(200));
        quotation2.setQuoteDate(LocalDate.of(2025, 2, 10));

        List<Quotation> mapped = List.of(quotation, quotation2);
        List<QuotationResponseDTO> responseDtos = List.of(responseDto, responseDto);

        when(quotationRequestMapper.toEntity(requestDtos)).thenReturn(mapped);
        when(orderRepository.getReferenceById(orderId)).thenReturn(order);
        when(quotationRepository.findByOrderId(orderId)).thenReturn(List.of());
        when(quotationRepository.saveAll(mapped)).thenReturn(mapped);
        when(quotationResponseMapper.toDto(mapped)).thenReturn(responseDtos);

        ResponseEntity<List<QuotationResponseDTO>> response = quotationService.createQuotation(orderId, requestDtos);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(responseDtos, response.getBody());

        ArgumentCaptor<List<Quotation>> captor = ArgumentCaptor.forClass(List.class);
        verify(quotationRepository).saveAll(captor.capture());
        List<Quotation> saved = captor.getValue();

        assertEquals(new QuotationId(orderId, (short) 1), saved.get(0).getId());
        assertEquals(new QuotationId(orderId, (short) 2), saved.get(1).getId());
        assertSame(order, saved.get(0).getOrder());
        assertSame(order, saved.get(1).getOrder());

        verify(quotationRequestMapper).toEntity(requestDtos);
        verify(orderRepository).getReferenceById(orderId);
        verify(quotationRepository).findByOrderId(orderId);
        verify(quotationResponseMapper).toDto(mapped);
    }

    @Test
    void should_start_index_after_existing_quotations() {
        Long orderId = 100L;
        List<QuotationRequestDTO> requestDtos = List.of(requestDto);

        Quotation existing = new Quotation();
        existing.setId(new QuotationId(orderId, (short) 1));
        existing.setOrder(order);

        when(quotationRequestMapper.toEntity(requestDtos)).thenReturn(List.of(quotation));
        when(orderRepository.getReferenceById(orderId)).thenReturn(order);
        when(quotationRepository.findByOrderId(orderId)).thenReturn(List.of(existing));
        when(quotationRepository.saveAll(any(List.class))).thenReturn(List.of(quotation));
        when(quotationResponseMapper.toDto(any(List.class))).thenReturn(List.of(responseDto));

        ResponseEntity<List<QuotationResponseDTO>> response = quotationService.createQuotation(orderId, requestDtos);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(responseDto), response.getBody());

        ArgumentCaptor<List<Quotation>> captor = ArgumentCaptor.forClass(List.class);
        verify(quotationRepository).saveAll(captor.capture());
        Quotation saved = captor.getValue().get(0);

        assertEquals(new QuotationId(orderId, (short) 2), saved.getId());
        assertSame(order, saved.getOrder());
    }

    @Test
    void should_delete_quotation_and_return_no_content() {
        Long orderId = 100L;
        Short index = 1;

        ResponseEntity<String> response = quotationService.deleteQuotation(orderId, index);

        assertEquals(204, response.getStatusCode().value());
        verify(quotationRepository).deleteById(new QuotationId(orderId, index));
    }

    @Test
    void should_check_exists_quotation() {
        Long orderId = 100L;
        Short index = 1;

        when(quotationRepository.existsById(new QuotationId(orderId, index))).thenReturn(true);

        boolean exists = quotationService.existsQuotation(orderId, index);

        assertEquals(true, exists);
        verify(quotationRepository).existsById(new QuotationId(orderId, index));
    }
}
