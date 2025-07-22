package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.OrderRequestDTO;
import de.hs_esslingen.besy.dtos.response.OrderResponseDTO;
import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.mappers.request.ItemRequestMapper;
import de.hs_esslingen.besy.mappers.request.OrderRequestMapper;
import de.hs_esslingen.besy.mappers.request.QuotationRequestMapper;
import de.hs_esslingen.besy.mappers.response.OrderResponseMapper;
import de.hs_esslingen.besy.models.*;
import de.hs_esslingen.besy.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    private final ItemRequestMapper itemRequestMapper;
    private final QuotationRequestMapper quotationRequestMapper;


    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderResponseDTO> orderResponseDTOS = orderResponseMapper.toDto(orders);
        return ResponseEntity.ok(orderResponseDTOS);
    }


    public ResponseEntity<OrderResponseDTO> createOrder(OrderRequestDTO orderDTO) {
        // Now construct the order
        Order order = orderRequestMapper.toEntity(orderDTO);

        // Create entity proxies without null checks, since they must exist
        // Otherwise the transaction needs to fail
        User ownerRef = userRepository.getReferenceById(orderDTO.getOwnerId());
        Currency currencyRef = currencyRepository.getReferenceById(orderDTO.getCurrencyShort());
        Person deliveryPersonRef = personRepository.getReferenceById(orderDTO.getDeliveryPersonId());
        Person invoicePersonRef = personRepository.getReferenceById(orderDTO.getInvoicePersonId());
        Person queriesPersonRef = personRepository.getReferenceById(orderDTO.getQueriesPersonId());
        CostCenter secondaryCostCenterRef = costCenterRepository.getReferenceById(orderDTO.getSecondaryCostCenterId());

        CustomerIdId customerIdId = new CustomerIdId();
        customerIdId.setCustomerId(orderDTO.getCustomerId());
        customerIdId.setSupplierId(orderDTO.getSupplierId());
        CustomerId customerIdRef = customerIdRepository.getReferenceById(customerIdId);

        order.setOwner(ownerRef);
        order.setCurrency(currencyRef);
        order.setDeliveryPerson(deliveryPersonRef);
        order.setInvoicePerson(invoicePersonRef);
        order.setQueriesPerson(queriesPersonRef);
        order.setSecondaryCostCenter(secondaryCostCenterRef);
        order.setCustomer(customerIdRef);

        order.setStatus(orderDTO.getStatus());

        // TODO: Replace this.
        // Construct all items and manually map the composite primary key (ItemId)
        // This is a temporary workaround and should be replaced by proper REST endpoints in the future
        List<Item> items = itemRequestMapper.toEntity(orderDTO.getItems());
        items.forEach(item -> {
            ItemId itemId = new ItemId();
            itemId.setOrderId(item.getOrderId());
            itemId.setItemId(item.getItemId());
            item.setId(itemId);
            item.setOrder(order);
        });

        List<Quotation> quotations = quotationRequestMapper.toEntity(orderDTO.getQuotations());
        quotations.forEach(quotation -> {
            QuotationId quotationId = new QuotationId();
            quotationId.setOrderId(quotation.getOrderId());
            quotationId.setIndex(quotation.getIndex());
            quotation.setId(quotationId);
            quotation.setOrder(order);
        });

        // Persist them to DB
        // Order first, since item and quotation reference it
        orderRepository.save(order);
        itemRepository.saveAll(items);
        quotationRepository.saveAll(quotations);

        return ResponseEntity.ok().build();
    }



    public ResponseEntity<List<OrderResponseDTO>> getOrdersOfOwnerUser(String username) {
        List<Order> orders = orderRepository.findByOwnerId(username);
        List<OrderResponseDTO> orderResponseDTOS = orderResponseMapper.toDto(orders);
        return ResponseEntity.ok(orderResponseDTOS);
    }

}
