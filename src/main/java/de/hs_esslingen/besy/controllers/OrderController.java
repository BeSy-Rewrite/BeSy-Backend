package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.request.ItemRequestDTO;
import de.hs_esslingen.besy.dtos.request.OrderRequestDTO;
import de.hs_esslingen.besy.dtos.request.QuotationRequestDTO;
import de.hs_esslingen.besy.dtos.response.ItemResponseDTO;
import de.hs_esslingen.besy.dtos.response.OrderResponseDTO;
import de.hs_esslingen.besy.dtos.response.QuotationResponseDTO;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.services.ItemService;
import de.hs_esslingen.besy.services.OrderPDFService;
import de.hs_esslingen.besy.services.OrderService;
import de.hs_esslingen.besy.services.QuotationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    private final OrderService orderService;
    private final ItemService itemService;
    private final QuotationService quotationService;
    private final OrderPDFService orderPDFService;

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        return orderService.createOrder(orderRequestDTO);
    }

    @GetMapping("{order-id}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable("order-id") Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("{order-id}/items")
    public ResponseEntity<List<ItemResponseDTO>> getItemsOfOrder(@PathVariable("order-id") Long id) {
        if(!orderService.existsOrderById(id)) throw new NotFoundException("Bestellung nicht gefunden.");
        return itemService.getItemsOfOrder(id);
    }

    @PostMapping("{order-id}/items")
    public ResponseEntity<List<ItemResponseDTO>> createItemsOfOrder(
            @PathVariable("order-id") Long id,
            @RequestBody List<ItemRequestDTO> dtos) {
        if(!orderService.existsOrderById(id)) throw new NotFoundException("Bestellung nicht gefunden.");
        return itemService.createItemsOfOrder(id, dtos);
    }

    @GetMapping("{order-id}/quotations")
    public ResponseEntity<List<QuotationResponseDTO>> getQuotationsOfOrder(@PathVariable("order-id") Long id) {
        if(!orderService.existsOrderById(id)) throw new NotFoundException("Bestellung nicht gefunden.");
        return quotationService.getQuotationsOfOrder(id);
    }

    @PostMapping("{order-id}/quotations")
    public ResponseEntity<List<QuotationResponseDTO>> createQuotationsOfOrder(
            @PathVariable("order-id") Long id,
            @RequestBody List<QuotationRequestDTO> dtos
    ){
        return quotationService.createQuotation(id, dtos);
    }

    @GetMapping
    @RequestMapping("{order-id}/export")
    public ResponseEntity<byte[]> exportOrder(@PathVariable("order-id") Integer orderId) throws IOException {
        return this.orderPDFService.generateOrderPDF(orderId);
    }


}
