package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.OrderStatusResponseDTO;
import de.hs_esslingen.besy.mappers.response.OrderStatusResponseMapper;
import de.hs_esslingen.besy.models.OrderStatus;
import de.hs_esslingen.besy.repositories.OrderStatusRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderStatusService {

    private final OrderStatusRepository orderStatusRepository;
    private final OrderStatusResponseMapper orderStatusResponseMapper;

    public ResponseEntity<List<OrderStatusResponseDTO>> getAllOrderStatuses() {
        List<OrderStatus> orderStatuses = orderStatusRepository.findAll();
        List<OrderStatusResponseDTO> orderStatusResponseDTOS = orderStatusResponseMapper.toDto(orderStatuses);
        return ResponseEntity.ok(orderStatusResponseDTOS);
    }
}
