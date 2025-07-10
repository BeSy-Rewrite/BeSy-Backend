package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.OrderResponseDTO;
import de.hs_esslingen.besy.mapper.OrderResponseMapper;
import de.hs_esslingen.besy.model.*;
import de.hs_esslingen.besy.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final CurrencyRepository currencyRepository;
    private final PersonRepository personRepository;
    private final CostCenterRepository costCenterRepository;

    private final OrderResponseMapper orderResponseMapper;

    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderResponseDTO> orderResponseDTOS = orderResponseMapper.toDto(orders);
        return ResponseEntity.ok(orderResponseDTOS);
    }

}
