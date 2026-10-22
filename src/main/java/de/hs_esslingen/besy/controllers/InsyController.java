package de.hs_esslingen.besy.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.exceptions.BadRequestException;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.services.InsyService;
import de.hs_esslingen.besy.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "${api.prefix}/insy", produces = "application/json")
public class InsyController {

    private final InsyService insyService;
    private final OrderService orderService;

    @PostMapping("{order-id}")
    @Operation(description = "Sendet eine Bestellung anhand der Bestell-ID an das Insy-System.")
    @ApiResponse(responseCode = "200", description = "Bestellung erfolgreich an Insy gesendet.")
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<String> sendOrderToInsy(
            @Parameter(description = "ID der zu sendenden Bestellung.") @PathVariable("order-id") Long orderId) {
        if (!orderService.existsOrderById(orderId))
            throw new NotFoundException("Bestellung nicht gefunden.");
        if (!orderService.isOrderStatusEqual(orderId, List.of(OrderStatus.SETTLED, OrderStatus.ARCHIVED)))
            throw new BadRequestException("Bestellstatus befindet sich nicht auf fertiggestellt!");
        return insyService.createOrder(orderId);
    }
}
