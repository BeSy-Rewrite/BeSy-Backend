package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.services.InsyService;
import de.hs_esslingen.besy.services.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/insy")
public class InsyController {

    private final InsyService insyService;
    private final OrderService orderService;

    @PostMapping("{order-id}")
    public ResponseEntity<String> postOrderToInsy(@PathVariable("order-id") Long orderId) {
        if(!orderService.existsOrderById(orderId)) throw new NotFoundException("Bestellung nicht gefunden.");
        return insyService.createOrder(orderId);
    }
}
