package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.response.OrderStatusResponseDTO;
import de.hs_esslingen.besy.services.OrderStatusService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/order_status")
public class OrderStatusController {

    private final OrderStatusService orderStatusService;

    @GetMapping
    public ResponseEntity<List<OrderStatusResponseDTO>> getAllOrderStatuses() {
        return orderStatusService.getAllOrderStatuses();
    }
}
