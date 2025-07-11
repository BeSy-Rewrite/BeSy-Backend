package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.dto.request.OrderRequestDTO;
import de.hs_esslingen.besy.dto.response.OrderResponseDTO;
import de.hs_esslingen.besy.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        return orderService.createOrder(orderRequestDTO);
    }

    @PutMapping
    @RequestMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> patchOrder(
            @PathVariable("id") Long id,
            @RequestBody OrderRequestDTO orderRequestDTO
    ) {
        return orderService.updateOrder(id, orderRequestDTO);
    }

    @GetMapping
    @RequestMapping("/user/{owner-username}")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(@PathVariable("owner-username") String ownerUsername) {
        return orderService.getOrdersOfOwnerUser(ownerUsername);
    }
}
