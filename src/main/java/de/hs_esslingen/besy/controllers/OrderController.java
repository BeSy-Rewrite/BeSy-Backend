package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.request.OrderRequestDTO;
import de.hs_esslingen.besy.dtos.response.OrderResponseDTO;
import de.hs_esslingen.besy.services.OrderService;
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


    @GetMapping
    @RequestMapping("/user/{owner-username}")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(@PathVariable("owner-username") String ownerUsername) {
        return orderService.getOrdersOfOwnerUser(ownerUsername);
    }
}
