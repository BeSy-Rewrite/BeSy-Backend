package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.configurations.ValidationHelper;
import de.hs_esslingen.besy.dtos.request.OrderRequestDTO;
import de.hs_esslingen.besy.dtos.response.OrderResponseDTO;
import de.hs_esslingen.besy.dtos.response.OrderStatusHistoryResponseDTO;
import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.exceptions.BadRequestException;
import de.hs_esslingen.besy.exceptions.NotAuthorizedException;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.interfaces.OrderCompletedValidationDAO;
import de.hs_esslingen.besy.mappers.OrderCompletedValidationMapper;
import de.hs_esslingen.besy.mappers.request.OrderRequestMapper;
import de.hs_esslingen.besy.mappers.response.OrderResponseMapper;
import de.hs_esslingen.besy.mappers.response.OrderStatusHistoryResponseMapper;
import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.models.CostCenter;
import de.hs_esslingen.besy.models.CustomerId;
import de.hs_esslingen.besy.models.CustomerIdId;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.OrderStatusHistory;
import de.hs_esslingen.besy.models.Person;
import de.hs_esslingen.besy.models.User;
import de.hs_esslingen.besy.models.Currency;
import de.hs_esslingen.besy.repositories.AddressRepository;
import de.hs_esslingen.besy.repositories.CostCenterRepository;
import de.hs_esslingen.besy.repositories.CurrencyRepository;
import de.hs_esslingen.besy.repositories.CustomerIdRepository;
import de.hs_esslingen.besy.repositories.OrderPageableRepository;
import de.hs_esslingen.besy.repositories.OrderRepository;
import de.hs_esslingen.besy.repositories.OrderStatusHistoryRepository;
import de.hs_esslingen.besy.repositories.PersonRepository;
import de.hs_esslingen.besy.repositories.UserRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderPageableRepository orderPageableRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private CostCenterRepository costCenterRepository;

    @Mock
    private CustomerIdRepository customerIdRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Mock
    private OrderResponseMapper orderResponseMapper;

    @Mock
    private OrderRequestMapper orderRequestMapper;

    @Mock
    private OrderCompletedValidationMapper orderCompletedValidationMapper;

    @Mock
    private OrderStatusHistoryResponseMapper orderStatusHistoryResponseMapper;

    @Mock
    private ValidationHelper validator;

    @InjectMocks
    private OrderService orderService;
    private Order order;
    private OrderRequestDTO requestDto;
    private OrderResponseDTO responseDto;
    private Jwt jwtWithRole;
    private Jwt jwtWithoutRole;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "dekanRoleName", "dekan");
        ReflectionTestUtils.setField(de.hs_esslingen.besy.security.KeycloakAuthenticationConverter.class, "clientId", "test-client");

        order = new Order();
        order.setId(1L);
        order.setPrimaryCostCenterId("CC-1");
        order.setBookingYear("25");
        order.setAutoIndex((short) 1);
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setOwnerId(1);
        order.setSupplierId(2);

        requestDto = new OrderRequestDTO(
                "CC-1",
                "25",
                "LA",
                1,
                "Content",
                "EUR",
                "Comment",
                "Comment Supplier",
                "Q-1",
                "QS",
                null,
                BigDecimal.valueOf(100),
                10L,
                11L,
                12L,
                "CUST-1",
                2,
                "CC-2",
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(0),
                (short) 0,
                false,
                false,
                false,
                false,
                false,
                false,
                "",
                "DFG",
                100,
                101
        );

        responseDto = new OrderResponseDTO(
                1L,
                "CC-1",
                "25",
                (short) 1,
                null,
                "LA",
                1,
                "Content",
                OrderStatus.IN_PROGRESS,
                null,
                "Comment",
                "Comment Supplier",
                "Q-1",
                "QS",
                null,
                BigDecimal.valueOf(100),
                10L,
                11L,
                12L,
                "CUST-1",
                2,
                "CC-2",
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(0),
                (short) 0,
                null,
                false,
                false,
                false,
                false,
                false,
                false,
                "",
                "DFG",
                100,
                101
        );

        jwtWithRole = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-123")
                .claim("resource_access", Map.of("test-client", Map.of("roles", List.of("dekan"))))
                .build();

        jwtWithoutRole = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-123")
                .claim("resource_access", Map.of("test-client", Map.of("roles", List.of("user"))))
                .build();
    }

    @Test
    void should_get_all_orders_with_pagination_and_filters() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);

        when(orderPageableRepository.findAll(any(), eq(pageable))).thenReturn(page);
        when(orderResponseMapper.toDto(order)).thenReturn(responseDto);

        Page<OrderResponseDTO> result = orderService.getAllOrders(
                null,
                List.of("25"),
                null,
                null,
                List.of(1),
                List.of(OrderStatus.IN_PROGRESS),
                null,
                null,
                null,
                null,
                null,
                List.of("CUST-1"),
                List.of(2),
                List.of("CC-2"),
                null,
                null,
                null,
                null,
                pageable
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(responseDto, result.getContent().get(0));
        verify(orderPageableRepository).findAll(any(), eq(pageable));
        verify(orderResponseMapper).toDto(order);
    }

    @Test
    void should_get_order_by_id_when_exists() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderResponseMapper.toDto(order)).thenReturn(responseDto);

        ResponseEntity<OrderResponseDTO> response = orderService.getOrderById(1L);

        assertSame(responseDto, response.getBody());
        verify(orderRepository).findById(1L);
        verify(orderResponseMapper).toDto(order);
    }

    @Test
    void should_throw_not_found_when_get_order_by_id_missing() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.getOrderById(1L));
        verify(orderRepository).findById(1L);
    }

    @Test
    void should_create_order_with_auto_index_first_order_and_history_entry() {
        Order mapped = new Order();
        mapped.setPrimaryCostCenterId("CC-1");
        mapped.setBookingYear("25");

        when(orderRequestMapper.toEntity(requestDto)).thenReturn(mapped);
        when(orderRepository.findTopByPrimaryCostCenterIdAndBookingYearOrderByAutoIndexDesc("CC-1", "25"))
                .thenReturn(null);
        when(orderRepository.save(mapped)).thenReturn(mapped);
        when(orderResponseMapper.toDto(mapped)).thenReturn(responseDto);

        ResponseEntity<OrderResponseDTO> response = orderService.createOrder(requestDto, null);

        assertSame(responseDto, response.getBody());
        assertEquals((short) 1, mapped.getAutoIndex());
        assertEquals(OrderStatus.IN_PROGRESS, mapped.getStatus());
        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    @Test
    void should_create_order_with_incremented_auto_index() {
        Order mapped = new Order();
        mapped.setPrimaryCostCenterId("CC-1");
        mapped.setBookingYear("25");

        Order latest = new Order();
        latest.setAutoIndex((short) 5);

        when(orderRequestMapper.toEntity(requestDto)).thenReturn(mapped);
        when(orderRepository.findTopByPrimaryCostCenterIdAndBookingYearOrderByAutoIndexDesc("CC-1", "25"))
                .thenReturn(latest);
        when(orderRepository.save(mapped)).thenReturn(mapped);
        when(orderResponseMapper.toDto(mapped)).thenReturn(responseDto);

        orderService.createOrder(requestDto, null);

        assertEquals((short) 6, mapped.getAutoIndex());
    }

    @Test
    void should_update_order() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderResponseMapper.toDto(order)).thenReturn(responseDto);

        ResponseEntity<OrderResponseDTO> response = orderService.updateOrder(requestDto, 1L);

        assertSame(responseDto, response.getBody());
        verify(orderRequestMapper).partialUpdate(order, requestDto);
        verify(orderRepository).save(order);
    }

    @Test
    void should_delete_order_by_setting_status_deleted() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        ResponseEntity<String> response = orderService.deleteOrderById(1L);

        assertEquals(204, response.getStatusCode().value());
        assertEquals(OrderStatus.DELETED, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void should_return_no_content_when_deleting_nonexistent_order() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = orderService.deleteOrderById(1L);

        assertEquals(204, response.getStatusCode().value());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void should_update_order_status_for_allowed_transitions() {
        OrderStatusHistory history = new OrderStatusHistory();
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderStatusHistoryRepository.save(any(OrderStatusHistory.class))).thenReturn(history);

        OrderService.getOrderStatusMatrix().forEach((current, allowedTargets) -> {
            if (allowedTargets.isEmpty()) {
                return;
            }
            allowedTargets.forEach(target -> {
                Order currentOrder = new Order();
                currentOrder.setId(1L);
                currentOrder.setStatus(current);
                currentOrder.setPrimaryCostCenterId("CC-1");
                currentOrder.setBookingYear("25");
                currentOrder.setAutoIndex((short) 1);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(currentOrder));
                if (current == OrderStatus.IN_PROGRESS && target == OrderStatus.COMPLETED) {
                    when(orderCompletedValidationMapper.toEntity(currentOrder)).thenReturn(new OrderCompletedValidationDAO(
                            1L, "CC-1", "25", (short) 1, null, null, 1, "desc", OrderStatus.IN_PROGRESS,
                            "EUR", null, null, null, null, null, null, 1L, 1L, 1L,
                            "CUST", 1, "CC-2", null, null, null, null, null,
                            null, null, null, null, null, null, null, null,
                            null, 1
                    ));
                }

                Jwt jwt = (current == OrderStatus.DEKAN_PENDING && (target == OrderStatus.APPROVED || target == OrderStatus.COMPLETED))
                        ? jwtWithRole
                        : null;

                ResponseEntity<OrderStatus> response = orderService.updateOrderStatus(1L, target, jwt);
                assertEquals(target, response.getBody());
            });
        });
    }

    @Test
    void should_throw_bad_request_for_invalid_transition() {
        order.setStatus(OrderStatus.IN_PROGRESS);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> orderService.updateOrderStatus(1L, OrderStatus.APPROVED, null));
    }

    @Test
    void should_validate_completed_transition_and_throw_on_validation_failure() {
        order.setStatus(OrderStatus.IN_PROGRESS);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderCompletedValidationMapper.toEntity(order)).thenReturn(new OrderCompletedValidationDAO(
                1L, "CC-1", "25", (short) 1, null, null, 1, "desc", OrderStatus.IN_PROGRESS,
                "EUR", null, null, null, null, null, null, 1L, 1L, 1L,
                "CUST", 1, "CC-2", null, null, null, null, null,
                null, null, null, null, null, null, null, null,
                null, 1
        ));
        doThrow(new ConstraintViolationException(null)).when(validator).validateOrThrow(any(OrderCompletedValidationDAO.class));

        assertThrows(ConstraintViolationException.class, () -> orderService.updateOrderStatus(1L, OrderStatus.COMPLETED, null));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void should_throw_not_authorized_when_approving_without_role() {
        order.setStatus(OrderStatus.DEKAN_PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(NotAuthorizedException.class, () -> orderService.updateOrderStatus(1L, OrderStatus.APPROVED, jwtWithoutRole));
    }

    @Test
    void should_throw_not_authorized_when_returning_to_completed_without_role() {
        order.setStatus(OrderStatus.DEKAN_PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(NotAuthorizedException.class, () -> orderService.updateOrderStatus(1L, OrderStatus.COMPLETED, jwtWithoutRole));
    }

    @Test
    void should_approve_from_dekan_pending_with_dekan_role() {
        order.setStatus(OrderStatus.DEKAN_PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        ResponseEntity<OrderStatus> response = orderService.updateOrderStatus(1L, OrderStatus.APPROVED, jwtWithRole);

        assertEquals(OrderStatus.APPROVED, response.getBody());
        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    @Test
    void should_return_to_completed_from_dekan_pending_with_dekan_role() {
        order.setStatus(OrderStatus.DEKAN_PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        ResponseEntity<OrderStatus> response = orderService.updateOrderStatus(1L, OrderStatus.COMPLETED, jwtWithRole);

        assertEquals(OrderStatus.COMPLETED, response.getBody());
        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    @Test
    void should_transition_directly_from_completed_to_approved_without_dekan_role() {
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        ResponseEntity<OrderStatus> response = orderService.updateOrderStatus(1L, OrderStatus.APPROVED, null);

        assertEquals(OrderStatus.APPROVED, response.getBody());
    }

    @Test
    void should_get_status_history() {
        List<OrderStatusHistory> history = List.of(new OrderStatusHistory());
        List<OrderStatusHistoryResponseDTO> responseHistory = List.of(new OrderStatusHistoryResponseDTO(
                OrderStatus.IN_PROGRESS, null
        ));

        when(orderStatusHistoryRepository.findAllByOrderId(1L)).thenReturn(history);
        when(orderStatusHistoryResponseMapper.toDto(history)).thenReturn(responseHistory);

        ResponseEntity<List<OrderStatusHistoryResponseDTO>> response = orderService.getStatusHistory(1L);

        assertEquals(responseHistory, response.getBody());
        verify(orderStatusHistoryRepository).findAllByOrderId(1L);
        verify(orderStatusHistoryResponseMapper).toDto(history);
    }

    @Test
    void should_exists_order_by_id_when_not_deleted() {
        when(orderRepository.existsByIdAndStatusNot(1L, OrderStatus.DELETED)).thenReturn(true);

        assertTrue(orderService.existsOrderById(1L));
        verify(orderRepository).existsByIdAndStatusNot(1L, OrderStatus.DELETED);
    }

    @Test
    void should_is_order_status_equal_single_and_list_and_set() {
        order.setStatus(OrderStatus.APPROVED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertTrue(orderService.isOrderStatusEqual(1L, OrderStatus.APPROVED));
        assertTrue(orderService.isOrderStatusEqual(1L, List.of(OrderStatus.APPROVED, OrderStatus.SENT)));

        when(orderRepository.findById(2L)).thenReturn(Optional.empty());
        assertFalse(orderService.isOrderStatusEqual(2L, Set.of(OrderStatus.APPROVED)));
    }

    @Test
    void should_throw_when_is_order_status_equal_with_missing_order() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> orderService.isOrderStatusEqual(1L, OrderStatus.IN_PROGRESS));
        assertThrows(NoSuchElementException.class, () -> orderService.isOrderStatusEqual(1L, List.of(OrderStatus.IN_PROGRESS)));
    }

    @Test
    void should_get_statuses_allowing_transition_to() {
        Set<OrderStatus> result = orderService.getStatusesAllowingTransitionTo(OrderStatus.APPROVED);
        assertEquals(Set.of(OrderStatus.COMPLETED, OrderStatus.DEKAN_PENDING), result);
    }

    @Test
    void should_map_foreign_relationships_with_all_ids() {
        Order mapped = new Order();
        OrderRequestDTO dto = requestDto;

        when(orderRequestMapper.toEntity(dto)).thenReturn(mapped);
        when(orderRepository.findTopByPrimaryCostCenterIdAndBookingYearOrderByAutoIndexDesc("CC-1", "25"))
                .thenReturn(null);
        when(orderRepository.save(mapped)).thenReturn(mapped);
        when(orderResponseMapper.toDto(mapped)).thenReturn(responseDto);

        when(currencyRepository.getReferenceById("EUR")).thenReturn(new Currency());
        when(personRepository.getReferenceById(10L)).thenReturn(new Person());
        when(personRepository.getReferenceById(11L)).thenReturn(new Person());
        when(personRepository.getReferenceById(12L)).thenReturn(new Person());
        when(costCenterRepository.getReferenceById("CC-2")).thenReturn(new CostCenter());
        when(userRepository.getReferenceById(1)).thenReturn(new User());
        when(customerIdRepository.getReferenceById(new CustomerIdId("CUST-1", 2))).thenReturn(new CustomerId());
        when(addressRepository.getReferenceById(100)).thenReturn(new Address());
        when(addressRepository.getReferenceById(101)).thenReturn(new Address());
        when(costCenterRepository.getReferenceById("CC-1")).thenReturn(new CostCenter());

        orderService.createOrder(dto, null);

        verify(currencyRepository).getReferenceById("EUR");
        verify(personRepository).getReferenceById(10L);
        verify(personRepository).getReferenceById(11L);
        verify(personRepository).getReferenceById(12L);
        verify(costCenterRepository).getReferenceById("CC-2");
        verify(userRepository).getReferenceById(1);
        verify(customerIdRepository).getReferenceById(new CustomerIdId("CUST-1", 2));
        verify(addressRepository).getReferenceById(100);
        verify(addressRepository).getReferenceById(101);
        verify(costCenterRepository).getReferenceById("CC-1");
    }

    @Test
    @Disabled
    void should_map_owner_from_jwt_when_owner_id_null() {
        OrderRequestDTO dto = new OrderRequestDTO(
                "CC-1",
                "25",
                null,
                null,
                "Content",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                2,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        Order mapped = new Order();
        when(orderRequestMapper.toEntity(dto)).thenReturn(mapped);
        when(orderRepository.findTopByPrimaryCostCenterIdAndBookingYearOrderByAutoIndexDesc("CC-1", "25"))
                .thenReturn(null);
        when(orderRepository.save(mapped)).thenReturn(mapped);
        when(orderResponseMapper.toDto(mapped)).thenReturn(responseDto);

        User jwtUser = new User();
        when(userRepository.findByKeycloakUUID("user-123")).thenReturn(jwtUser);

        orderService.createOrder(dto, jwtWithRole);

        assertSame(jwtUser, mapped.getOwner());
        verify(userRepository).findByKeycloakUUID("user-123");
    }

    @Test
    void should_not_map_foreign_relationships_when_ids_null() {
        OrderRequestDTO dto = new OrderRequestDTO(
                null, null, null, null, "Content", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null
        );
        Order mapped = new Order();
        when(orderRequestMapper.toEntity(dto)).thenReturn(mapped);
        when(orderRepository.findTopByPrimaryCostCenterIdAndBookingYearOrderByAutoIndexDesc(null, null))
                .thenReturn(null);
        when(orderRepository.save(mapped)).thenReturn(mapped);
        when(orderResponseMapper.toDto(mapped)).thenReturn(responseDto);

        orderService.createOrder(dto, null);

        verifyNoInteractions(currencyRepository, personRepository, costCenterRepository, customerIdRepository, addressRepository, userRepository);
    }

    @Test
    void should_return_false_when_exists_by_id_and_status_not_for_deleted() {
        when(orderRepository.existsByIdAndStatusNot(1L, OrderStatus.DELETED)).thenReturn(false);
        assertFalse(orderService.existsOrderById(1L));
    }

    @Test
    void should_update_status_and_create_history_entry() {
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        ResponseEntity<OrderStatus> response = orderService.updateOrderStatus(1L, OrderStatus.DEKAN_PENDING, null);

        assertEquals(OrderStatus.DEKAN_PENDING, response.getBody());
        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    @Test
    @Disabled
    void should_generate_auto_index_on_completed_transition() {
        order.setStatus(OrderStatus.COMPLETED);
        order.setPrimaryCostCenterId("CC-1");
        order.setBookingYear("25");

        Order latest = new Order();
        latest.setAutoIndex((short) 2);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.findTopByPrimaryCostCenterIdAndBookingYearOrderByAutoIndexDesc("CC-1", "25"))
                .thenReturn(latest);
        when(orderRepository.save(order)).thenReturn(order);

        orderService.updateOrderStatus(1L, OrderStatus.COMPLETED, null);

        assertEquals((short) 3, order.getAutoIndex());
    }
}
