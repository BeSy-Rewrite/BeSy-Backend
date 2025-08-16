package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.OrderRequestDTO;
import de.hs_esslingen.besy.dtos.response.OrderResponseDTO;
import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.request.ItemRequestMapper;
import de.hs_esslingen.besy.mappers.request.OrderRequestMapper;
import de.hs_esslingen.besy.mappers.request.QuotationRequestMapper;
import de.hs_esslingen.besy.mappers.response.OrderResponseMapper;
import de.hs_esslingen.besy.models.*;
import de.hs_esslingen.besy.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final PersonRepository personRepository;
    private final CostCenterRepository costCenterRepository;
    private final CustomerIdRepository customerIdRepository;
    private final ItemRepository itemRepository;
    private final QuotationRepository quotationRepository;

    private final OrderResponseMapper orderResponseMapper;
    private final OrderRequestMapper orderRequestMapper;


    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderResponseDTO> orderResponseDTOS = orderResponseMapper.toDto(orders);
        return ResponseEntity.ok(orderResponseDTOS);
    }

    public ResponseEntity<OrderResponseDTO> getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    return ResponseEntity.ok(orderResponseMapper.toDto(order));
                }).orElseThrow(() -> new NotFoundException("Bestellung mit id " + id + " nicht gefunden."));
    }

    public ResponseEntity<OrderResponseDTO> createOrder(OrderRequestDTO dto) {
        Order order = orderRequestMapper.toEntity(dto);

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


        CostCenter costCenter = costCenterRepository.getReferenceById(dto.getPrimaryCostCenterId());
        order.setPrimaryCostCenter(costCenter);
        order.setBookingYear(dto.getBookingYear());

        Order latestAutoIndexOrder = orderRepository.findTopByPrimaryCostCenterIdAndBookingYearOrderByAutoIndexDesc(dto.getPrimaryCostCenterId(), dto.getBookingYear());
        Short latestAutoIndex = latestAutoIndexOrder.getAutoIndex();
        order.setAutoIndex(++latestAutoIndex);

        order.setLastUpdatedTime(OffsetDateTime.now());
        order.setStatus(OrderStatus.INB);
        return ResponseEntity.ok(orderResponseMapper.toDto(orderRepository.save(order)));

    }

    public boolean existsOrderById(Long id) {
        return orderRepository.existsById(id);
    }


}
