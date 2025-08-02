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


    public ResponseEntity<List<OrderResponseDTO>> getOrdersOfOwnerUser(String username) {
        List<Order> orders = orderRepository.findByOwnerId(username);
        List<OrderResponseDTO> orderResponseDTOS = orderResponseMapper.toDto(orders);
        return ResponseEntity.ok(orderResponseDTOS);
    }

}
