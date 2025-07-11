package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.response.OrderStatusResponseDTO;
import de.hs_esslingen.besy.mapper.response.OrderStatusResponseMapper;
import de.hs_esslingen.besy.model.OrderStatus;
import de.hs_esslingen.besy.repository.OrderStatusRepository;
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
