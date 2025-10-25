package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.configurations.SpecificationHelper;
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
import de.hs_esslingen.besy.models.*;
import de.hs_esslingen.besy.models.Currency;
import de.hs_esslingen.besy.repositories.*;
import de.hs_esslingen.besy.security.KeycloakAuthenticationConverter;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderPageableRepository orderPageableRepository;
    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final PersonRepository personRepository;
    private final CostCenterRepository costCenterRepository;
    private final CustomerIdRepository customerIdRepository;
    private final AddressRepository addressRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    private final OrderResponseMapper orderResponseMapper;
    private final OrderRequestMapper orderRequestMapper;
    private final OrderCompletedValidationMapper orderCompletedValidationMapper;
    private final OrderStatusHistoryResponseMapper orderStatusHistoryResponseMapper;

    private final ValidationHelper validator;


    /**
     * Defines valid status transitions for orders.
     * Each entry maps a current {@link OrderStatus} to a set of allowed next statuses.
     */
    private static final Map<OrderStatus, Set<OrderStatus>> ORDER_STATUS_MATRIX = Map.of(
            OrderStatus.IN_PROGRESS, Set.of(OrderStatus.COMPLETED, OrderStatus.DELETED),
            OrderStatus.COMPLETED, Set.of(OrderStatus.APPROVALS_RECEIVED, OrderStatus.IN_PROGRESS, OrderStatus.DELETED, OrderStatus.APPROVED),
            OrderStatus.APPROVALS_RECEIVED, Set.of(OrderStatus.APPROVED, OrderStatus.DELETED),
            OrderStatus.APPROVED, Set.of(OrderStatus.SENT),
            OrderStatus.REJECTED, Set.of(),
            OrderStatus.SENT, Set.of(OrderStatus.SETTLED),
            OrderStatus.SETTLED, Set.of(OrderStatus.ARCHIVED),
            OrderStatus.ARCHIVED, Set.of(),
            OrderStatus.DELETED, Set.of(OrderStatus.IN_PROGRESS)
    );


    public Page<OrderResponseDTO> getAllOrders(
            List<String> primaryCostCentersIds,
            List<String> bookingYears,
            OffsetDateTime createdAfter,
            OffsetDateTime createdBefore,
            List<Integer> ownerIds,
            List<OrderStatus> statuses,
            BigDecimal quotePriceMin,
            BigDecimal quotePriceMax,
            List<Long> deliveryPersonIds,
            List<Long> invoicePersonIds,
            List<Long> queriesPersonIds,
            List<String> customerIds,
            List<Integer> supplierIds,
            List<String> secondaryCostCenterIds,
            OffsetDateTime lastUpdatedTimeAfter,
            OffsetDateTime lastUpdatedTimeBefore,
            Short autoIndexGTE,
            Short autoIndexLTE,
            Pageable pageable

    ) {
        org.springframework.data.jpa.domain.Specification<Order> spec =
                SpecificationHelper
                        .contains(primaryCostCentersIds, "primaryCostCenterId")
                        .and(SpecificationHelper.contains(bookingYears, "bookingYear")
                        .and(SpecificationHelper.isBetween(createdAfter, createdBefore, "createdDate"))
                        .and(SpecificationHelper.contains(ownerIds, "ownerId"))
                        .and(SpecificationHelper.contains(statuses, "status"))
                        .and(SpecificationHelper.isBetween(quotePriceMin, quotePriceMax, "quotePrice"))
                        .and(SpecificationHelper.contains(deliveryPersonIds, "deliveryPersonId"))
                        .and(SpecificationHelper.contains(invoicePersonIds, "invoicePersonId"))
                        .and(SpecificationHelper.contains(queriesPersonIds, "queriesPersonId"))
                        .and(SpecificationHelper.contains(customerIds, "customerId"))
                        .and(SpecificationHelper.contains(supplierIds, "supplierId"))
                        .and(SpecificationHelper.contains(secondaryCostCenterIds, "secondaryCostCenterId"))
                        .and(SpecificationHelper.isBetween(lastUpdatedTimeAfter, lastUpdatedTimeBefore, "lastUpdatedTime"))
                        .and(SpecificationHelper.isBetween(autoIndexGTE, autoIndexLTE, "autoIndex"))
                                );

        Page<Order> orders = orderPageableRepository.findAll(spec, pageable);
        return orders.map(orderResponseMapper::toDto);
    }

    public ResponseEntity<OrderResponseDTO> getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    return ResponseEntity.ok(orderResponseMapper.toDto(order));
                }).orElseThrow(() -> new NotFoundException("Bestellung mit id " + id + " nicht gefunden."));
    }

    public ResponseEntity<OrderResponseDTO> createOrder(OrderRequestDTO dto, Jwt jwt) {
        Order order = orderRequestMapper.toEntity(dto);

        this.mapForeignRelationships(order, dto, jwt);

        order.setStatus(OrderStatus.IN_PROGRESS); // Override OrderStatus of DTO
        return ResponseEntity.ok(orderResponseMapper.toDto(orderRepository.save(order)));

    }

    public ResponseEntity<OrderResponseDTO> updateOrder(OrderRequestDTO dto, Long id) {
        Order order = orderRepository.findById(id).get();
        orderRequestMapper.partialUpdate(order, dto);
        this.mapForeignRelationships(order, dto, null);
        return ResponseEntity.ok(orderResponseMapper.toDto(orderRepository.save(order)));
    }


    public ResponseEntity<String> deleteOrderById(Long id) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if(orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(OrderStatus.DELETED);
            orderRepository.save(order);
        }
        return ResponseEntity.noContent().build();
    }



    /**
     * Updates the status of an existing order after validating the status transition.
     * Performs any necessary validation before persisting the updated order.
     *
     * @param id         the ID of the order to update
     * @param newStatus  the new {@link OrderStatus} to assign to the order
     * @return a {@link ResponseEntity} containing the updated order status
     *
     * @throws NoSuchElementException       if no order with the given ID is found
     * @throws BadRequestException        if the transition from the current to the new status is not allowed
     * @throws ConstraintViolationException if the updated order violates validation constraints
     */
    public ResponseEntity<OrderStatus> updateOrderStatus(Long id, OrderStatus newStatus, Jwt jwt) {
        Order order = orderRepository.findById(id).get();

        this.validateStatusTransition(order, newStatus, jwt);

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        OrderStatusHistory orderStatusHistory = new OrderStatusHistory();
        orderStatusHistory.setOrder(savedOrder);
        orderStatusHistory.setStatus(savedOrder.getStatus());

        if(isOrderStatusEqual(id, OrderStatus.COMPLETED)) {
            // Order got validated and is completed: set autoIndex
            generateOrderAutoIndex(order);
            if(order.getAutoIndex() == null) throw new RuntimeException("Fehler beim Generieren des Bestellnummer (autoIndex)!");
        }

        orderStatusHistoryRepository.save(orderStatusHistory);

        return ResponseEntity.ok(savedOrder.getStatus());
    }


    public ResponseEntity<List<OrderStatusHistoryResponseDTO>> getStatusHistory(Long orderId) {
        List<OrderStatusHistory> statusHistory = orderStatusHistoryRepository.findAllByOrderId(orderId);
        return ResponseEntity.ok(orderStatusHistoryResponseMapper.toDto(statusHistory));
    }


    /**
     * Checks if an order with the given ID exists and its status is not 'DEL' (deleted).
     *
     * @param id the ID of the order to check
     * @return true if such an order exists and is not marked as deleted, false otherwise
     */
    public boolean existsOrderById(Long id) {
        return orderRepository.existsByIdAndStatusNot(id, OrderStatus.DELETED);
    }



    /**
     * Checks if the status of the order with the given ID matches the provided status.
     *
     * This method retrieves the order from the repository using the provided order ID and
     * compares the order's status with the given status. If the order is found and its status
     * matches the provided status, it returns true. Otherwise, it returns false.
     *
     * Note: This method assumes that the order with the given ID exists in the database. If
     *       the order is not found, it will throw a {@link NoSuchElementException} when trying to access
     *       the status.
     *
     * @param orderId The ID of the order to be checked.
     * @param status The status to compare the order's status against.
     * @return {@code true} if the order's status matches the given status, {@code false} otherwise.
     */
    public boolean isOrderStatusEqual(Long orderId, OrderStatus status) {
        return orderRepository.findById(orderId).get().getStatus().equals(status);
    }

    public boolean isOrderStatusEqual(Long orderId, List<OrderStatus> statusList) {
        OrderStatus orderStatus = orderRepository.findById(orderId).get().getStatus();
        return statusList.stream().anyMatch(orderStatus::equals);
    }


    /**
     * Checks if the status of the order with the given ID is contained within the provided list of statuses.
     *
     * @param orderId the ID of the order to check
     * @param statuses the list of OrderStatus values to check against
     * @return true if the order exists and its status is in the list, false otherwise
     */
    public boolean isOrderStatusEqual(Long orderId, Set<OrderStatus> statuses){
        return orderRepository.findById(orderId).map(order -> statuses.contains(order.getStatus())).orElse(false);
    }


    public Set<OrderStatus> getStatusesAllowingTransitionTo(OrderStatus status) {
        return ORDER_STATUS_MATRIX.entrySet().stream()
                .filter(entry -> entry.getValue().contains(status))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }


    /**
     * Checks whether a transition from the current order status to the target status is valid,
     * based on the defined {@code ORDER_STATUS_MATRIX}.
     *
     * @param current the current status of the order
     * @param target the desired target status
     * @return {@code true} if the transition is allowed; {@code false} otherwise
     */
    private boolean isValidStatusTransition(OrderStatus current, OrderStatus target) {
        return ORDER_STATUS_MATRIX.getOrDefault(current, Set.of()).contains(target);
    }


    private void validateStatusTransition(Order currentOrder, OrderStatus targetStatus, Jwt jwt) {
        OrderStatus currentStatus = currentOrder.getStatus();

        if (!isValidStatusTransition(currentStatus, targetStatus)) {
            throw new BadRequestException(String.format(
                    "Ungültiger Statusübergang von %s zu %s", currentStatus, targetStatus
            ));
        }

        // Validate all non-null fields are set when moving from IN_PROGRESS to COMPLETED
        if(currentStatus.equals(OrderStatus.IN_PROGRESS) && targetStatus.equals(OrderStatus.COMPLETED)) {
            OrderCompletedValidationDAO orderToBeValidated = orderCompletedValidationMapper.toEntity(currentOrder);
            validator.validateOrThrow(orderToBeValidated);
        }

        if(currentStatus.equals(OrderStatus.APPROVALS_RECEIVED) && targetStatus.equals(OrderStatus.APPROVED)) {
            if(!KeycloakAuthenticationConverter.hasRole(jwt, "dekan")) throw new NotAuthorizedException("Not authorized to modify this order!");
        }

    }


    private Order mapForeignRelationships(Order order, OrderRequestDTO dto, Jwt jwt){
        if(dto.getCurrencyShort() != null) {
            Currency currency = currencyRepository.getReferenceById(dto.getCurrencyShort());
            order.setCurrency(currency);
        }
        if(dto.getDeliveryPersonId() != null) {
            Person deliveryPerson = personRepository.getReferenceById(dto.getDeliveryPersonId());
            order.setDeliveryPerson(deliveryPerson);
        }
        if(dto.getInvoicePersonId() != null) {
            Person invoicePerson = personRepository.getReferenceById(dto.getInvoicePersonId());
            order.setInvoicePerson(invoicePerson);
        }
        if(dto.getQueriesPersonId() != null) {
            Person queriesPerson = personRepository.getReferenceById(dto.getQueriesPersonId());
            order.setQueriesPerson(queriesPerson);
        }
        if(dto.getSecondaryCostCenterId() != null) {
            CostCenter costCenter = costCenterRepository.getReferenceById(dto.getSecondaryCostCenterId());
            order.setSecondaryCostCenter(costCenter);
        }
        if(dto.getOwnerId() != null) {
            User user = userRepository.getReferenceById(dto.getOwnerId());
            order.setOwner(user);
        }
        if(dto.getCustomerId() != null) {
            CustomerId customerId = customerIdRepository.getReferenceById(
                    new CustomerIdId(dto.getCustomerId(), dto.getSupplierId())
            );
            order.setCustomer(customerId);
        }
        if(dto.getDeliveryAddressId() != null) {
            Address deliveryAddress = addressRepository.getReferenceById(dto.getDeliveryAddressId());
            order.setDeliveryAddress(deliveryAddress);
        }
        if(dto.getInvoiceAddressId() != null) {
            Address invoiceAddress = addressRepository.getReferenceById(dto.getInvoiceAddressId());
            order.setInvoiceAddress(invoiceAddress);
        }
        if(dto.getPrimaryCostCenterId() != null) {
            CostCenter costCenter = costCenterRepository.getReferenceById(dto.getPrimaryCostCenterId());
            order.setPrimaryCostCenter(costCenter);
        }
        if(dto.getOwnerId() == null && jwt != null) {
            User user = userRepository.findByKeycloakUUID(jwt.getSubject());
            if(user != null) order.setOwner(user);
        }
        return order;
    }


    public static Map<OrderStatus, Set<OrderStatus>> getOrderStatusMatrix(){
        return ORDER_STATUS_MATRIX;
    }


    private void generateOrderAutoIndex(Order order) {
        Order latestAutoIndexOrder = orderRepository.findTopByPrimaryCostCenterIdAndBookingYearOrderByAutoIndexDesc(order.getPrimaryCostCenterId(), order.getBookingYear());
        Short latestAutoIndex = latestAutoIndexOrder.getAutoIndex();

        if(latestAutoIndex != null) {
            order.setAutoIndex(++latestAutoIndex);
        } else{
            order.setAutoIndex((short) 1);
        }
    }

}
