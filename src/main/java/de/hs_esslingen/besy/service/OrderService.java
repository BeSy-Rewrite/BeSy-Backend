package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.request.OrderRequestDTO;
import de.hs_esslingen.besy.dto.response.OrderResponseDTO;
import de.hs_esslingen.besy.exception.NotFoundException;
import de.hs_esslingen.besy.mapper.request.OrderRequestMapper;
import de.hs_esslingen.besy.mapper.response.OrderResponseMapper;
import de.hs_esslingen.besy.model.*;
import de.hs_esslingen.besy.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderResponseMapper orderResponseMapper;
    private final OrderRequestMapper orderRequestMapper;
    private final UserRepository userRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final CurrencyRepository currencyRepository;
    private final PersonRepository personRepository;
    private final CostCenterRepository costCenterRepository;
    private final CustomerIdRepository customerIdRepository;

    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderResponseDTO> orderResponseDTOS = orderResponseMapper.toDto(orders);
        return ResponseEntity.ok(orderResponseDTOS);
    }


    public ResponseEntity<OrderResponseDTO> createOrder(OrderRequestDTO orderDTO) {
        Long id = orderDTO.getOrderId();
        if(orderRepository.existsById(id)) return ResponseEntity.status(HttpStatus.CONFLICT).build();
        return updateOrder(id, orderDTO);
    }


    /**
     * Completely updates an existing order with new data provided in the {@link OrderRequestDTO}.
     * <p>
     * This method performs the following steps:
     * <ul>
     *   <li>Validates that the order with the given ID exists; throws a {@link NotFoundException} if not.</li>
     *   <li>Fetches JPA references (proxies) for all related entities using {@code getReferenceById()}, improving
     *       performance by deferring database access until flush time.</li>
     *   <li>Manually constructs the composite key for the {@link CustomerId} reference and retrieves its proxy.</li>
     *   <li>Updates the order entity with the provided DTO values using the {@code partialUpdate} method.</li>
     *   <li>Saves the updated order and returns the response DTO wrapped in a {@link ResponseEntity}.</li>
     * </ul>
     *
     * @param id        the ID of the order to update
     * @param orderDTO  the DTO containing the fields to update in the order
     * @return          a {@link ResponseEntity} containing the updated order as a {@link OrderResponseDTO}
     * @throws NotFoundException if the order does not exist or any referenced entity is missing
     * @throws de.hs_esslingen.besy.exception.NotFoundException; if any referenced entity (e.g., User, Currency, etc.)
     *         does not exist in the database
     */

    // ⚠️ Warning: This logic is critical to the createOrder method.
    // ⚠️ Changes here may impact POST request processing — verify carefully.
    // As of now, they are identical.

    public ResponseEntity<OrderResponseDTO> updateOrder(Long id, OrderRequestDTO orderDTO) {
        // Ensure the order to be updated exists
        Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found."));

        // Obtain JPA proxies for all related entities using getReferenceById
        // These references must point to valid existing entities, otherwise an exception will be thrown at flush time
        // This approach avoids immediate database queries and improves performance by deferring data loading
        // Important: As long as those  references are NOT nullable, it can stay like this. Otherwise this needs to be changed, so proxies are only created if not null.
        User ownerRef = userRepository.getReferenceById(orderDTO.getOwnerUserName());
        OrderStatus orderstatusRef = orderStatusRepository.getReferenceById(orderDTO.getOrderStatus());
        Currency currencyRef = currencyRepository.getReferenceById(orderDTO.getCurrencyShort());
        Person deliveryPersonRef = personRepository.getReferenceById(orderDTO.getDeliveryPersonId());
        Person invoicePersonRef = personRepository.getReferenceById(orderDTO.getInvoicePersonId());
        Person queriesPersonRef = personRepository.getReferenceById(orderDTO.getQueriesPersonId());
        CostCenter costCenterRef = costCenterRepository.getReferenceById(orderDTO.getSecondaryCostCenterId());

        // Manually construct the composite primary key for CustomerId
        CustomerIdId customerIdId = new CustomerIdId();
        customerIdId.setSupplierName(orderDTO.getSupplierName());
        customerIdId.setCustomerId(orderDTO.getCustomerId());
        CustomerId customerIdRef = customerIdRepository.getReferenceById(customerIdId);

        order.setOwner(ownerRef);
        order.setOrderStatusRef(orderstatusRef);
        order.setCurrency(currencyRef);
        order.setDeliveryPerson(deliveryPersonRef);
        order.setInvoicePerson(invoicePersonRef);
        order.setQueriesPerson(queriesPersonRef);
        order.setSecondaryCostCenter(costCenterRef);
        order.setCustomer(customerIdRef);

        orderRequestMapper.partialUpdate(order, orderDTO);
        Order savedOrder = orderRepository.save(order);
        return ResponseEntity.ok(orderResponseMapper.toDto(savedOrder));
    }


    public ResponseEntity<List<OrderResponseDTO>> getOrdersOfOwnerUser(String username) {
        List<Order> orders = orderRepository.findByOwnerUserName(username);
        List<OrderResponseDTO> orderResponseDTOS = orderResponseMapper.toDto(orders);
        return ResponseEntity.ok(orderResponseDTOS);
    }

}
