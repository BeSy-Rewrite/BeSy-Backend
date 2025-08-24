package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.configurations.Specification;
import de.hs_esslingen.besy.dtos.request.OrderRequestDTO;
import de.hs_esslingen.besy.dtos.response.AddressResponseDTO;
import de.hs_esslingen.besy.dtos.response.OrderResponseDTO;
import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.exceptions.BadRequestException;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.request.OrderRequestMapper;
import de.hs_esslingen.besy.mappers.response.OrderResponseMapper;
import de.hs_esslingen.besy.models.*;
import de.hs_esslingen.besy.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

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

    private final OrderResponseMapper orderResponseMapper;
    private final OrderRequestMapper orderRequestMapper;
    private final AddressRepository addressRepository;


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
            Pageable pageable

    ) {
        org.springframework.data.jpa.domain.Specification<Order> spec =
                Specification
                        .contains(primaryCostCentersIds, "primaryCostCenterId")
                        .and(Specification.contains(bookingYears, "bookingYear")
                        .and(Specification.isBetween(createdAfter, createdBefore, "createdDate"))
                        .and(Specification.contains(ownerIds, "ownerId"))
                        .and(Specification.contains(statuses, "status"))
                        .and(Specification.isBetween(quotePriceMin, quotePriceMax, "quotePrice"))
                        .and(Specification.contains(deliveryPersonIds, "deliveryPersonId"))
                        .and(Specification.contains(invoicePersonIds, "invoicePersonId"))
                        .and(Specification.contains(queriesPersonIds, "queriesPersonId"))
                        .and(Specification.contains(customerIds, "customerId"))
                        .and(Specification.contains(supplierIds, "supplierId"))
                        .and(Specification.contains(secondaryCostCenterIds, "secondaryCostCenterId"))
                        .and(Specification.isBetween(lastUpdatedTimeAfter, lastUpdatedTimeBefore, "lastUpdatedTime"))
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

    public ResponseEntity<OrderResponseDTO> createOrder(OrderRequestDTO dto) {
        Order order = orderRequestMapper.toEntity(dto);

        this.mapForeignRelationships(order, dto);

        /*        Order latestAutoIndexOrder = orderRepository.findTopByPrimaryCostCenterIdAndBookingYearOrderByAutoIndexDesc(dto.getPrimaryCostCenterId(), dto.getBookingYear());
        Short latestAutoIndex = latestAutoIndexOrder.getAutoIndex();
        order.setAutoIndex(++latestAutoIndex);*/

        order.setStatus(OrderStatus.IN_PROGRESS); // Override OrderStatus of DTO
        return ResponseEntity.ok(orderResponseMapper.toDto(orderRepository.save(order)));

    }

    public ResponseEntity<OrderResponseDTO> updateOrder(OrderRequestDTO dto, Long id) {
        Order order = orderRepository.findById(id).get();
        orderRequestMapper.partialUpdate(order, dto);
        this.mapForeignRelationships(order, dto);
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



    private Order mapForeignRelationships(Order order, OrderRequestDTO dto){
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
        return order;
    }


}
