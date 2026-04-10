package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.enums.VatType;
import de.hs_esslingen.besy.helper.OrderNumberHelper;
import de.hs_esslingen.besy.models.*;
import de.hs_esslingen.besy.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsyServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private CostCenterRepository costCenterRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderPDFService pdfService;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private OrderNumberHelper orderNumberHelper;

    @InjectMocks
    private InsyService insyService;

    private Order order;
    private Supplier supplier;
    private CostCenter costCenter;
    private User user;
    private List<Item> items;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(insyService, "insyBaseUrl", "http://insy.test");
        ReflectionTestUtils.setField(insyService, "insyOrdersUrl", "/orders");
        ReflectionTestUtils.setField(insyService, "username", "user");
        ReflectionTestUtils.setField(insyService, "password", "pass");
        ReflectionTestUtils.setField(insyService, "insyClientName", "client");
        ReflectionTestUtils.setField(insyService, "authProtocol", "basic");

        order = new Order();
        order.setId(100L);
        order.setSupplierId(10);
        order.setPrimaryCostCenterId("CC-1");
        order.setOwnerId(20);
        order.setBookingYear("25");
        order.setAutoIndex((short) 1);
        order.setCreatedDate(LocalDateTime.of(2025, 1, 1, 10, 0));
        order.setContentDescription("Order description");
        order.setQuotePrice(BigDecimal.valueOf(100));

        supplier = new Supplier();
        supplier.setName("Supplier One");

        costCenter = new CostCenter();
        costCenter.setId("CC-1");
        costCenter.setName("Main Center");

        user = new User();
        user.setName("Jane");
        user.setSurname("Doe");

        Item item = new Item();
        item.setId(new ItemId(100L, 1));
        item.setName("Item A");
        item.setPricePerUnit(BigDecimal.valueOf(10));
        item.setVatType(VatType.brutto);
        item.setQuantity(2L);
        item.setMigratedToInsy(false);

        items = List.of(item);
    }

    @Test
    void should_create_order_return_api_response_and_mark_items_migrated() {
        Long orderId = 100L;
        String apiResponse = "OK";

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(supplierRepository.findById(order.getSupplierId())).thenReturn(Optional.of(supplier));
        when(costCenterRepository.findById(order.getPrimaryCostCenterId())).thenReturn(Optional.of(costCenter));
        when(userRepository.findById(order.getOwnerId())).thenReturn(Optional.of(user));
        when(itemRepository.findByOrder_Id(orderId)).thenReturn(items);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.attributes(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(List.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(apiResponse);
        when(orderNumberHelper.generateOrderNumber(any(Order.class))).thenReturn("IT200/26/001");

        ResponseEntity<String> response = insyService.createOrder(orderId);

        assertEquals(apiResponse, response.getBody());
        items.forEach(item -> assertEquals(true, item.getMigratedToInsy()));

        verify(orderRepository).findById(orderId);
        verify(supplierRepository).findById(order.getSupplierId());
        verify(costCenterRepository).findById(order.getPrimaryCostCenterId());
        verify(userRepository).findById(order.getOwnerId());
        verify(itemRepository).findByOrder_Id(orderId);
        verify(itemRepository).saveAll(items);

        verify(orderRepository, never()).delete(any());
        verify(supplierRepository, never()).delete(any());
        verify(costCenterRepository, never()).delete(any());
        verify(userRepository, never()).delete(any());
        verify(itemRepository, never()).delete(any());
    }
}
