package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.request.ApprovalRequestDTO;
import de.hs_esslingen.besy.dtos.request.ItemRequestDTO;
import de.hs_esslingen.besy.dtos.request.OrderRequestDTO;
import de.hs_esslingen.besy.dtos.request.QuotationRequestDTO;
import de.hs_esslingen.besy.dtos.response.ApprovalResponseDTO;
import de.hs_esslingen.besy.dtos.response.ItemResponseDTO;
import de.hs_esslingen.besy.dtos.response.OrderResponseDTO;
import de.hs_esslingen.besy.dtos.response.QuotationResponseDTO;
import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.exceptions.BadRequestException;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.services.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    private final OrderService orderService;
    private final ItemService itemService;
    private final QuotationService quotationService;
    private final OrderPDFService orderPDFService;
    private final ApprovalService approvalService;

    @GetMapping
    public Page<OrderResponseDTO> getAllOrders(
            @RequestParam(name = "primaryCostCenters", required = false) List<String> primaryCostCenterIds,
            @RequestParam(name = "bookingYears", required = false) List<String> bookingYears,
            @RequestParam(name = "createdAfter", required = false) OffsetDateTime createdAfter,
            @RequestParam(name = "createdBefore", required = false) OffsetDateTime createdBefore,
            @RequestParam(name = "ownerIds", required = false) List<Integer> ownerIds,
            @RequestParam(name = "statuses", required = false, defaultValue = "ABR,ABS,ARC,INB") List<OrderStatus> statuses,
            @RequestParam(name = "quotePriceMin", required = false) BigDecimal quotePriceMin,
            @RequestParam(name = "quotePriceMax", required = false) BigDecimal quotePriceMax,
            @RequestParam(name = "deliveryPersonIds", required = false) List<Long> deliveryPersonIds,
            @RequestParam(name = "invoicePersonIds", required = false) List<Long> invoicePersonIds,
            @RequestParam(name = "queriesPersonIds", required = false) List<Long> queriesPersonIds,
            @RequestParam(name = "customerIds", required = false) List<String> customerIds,
            @RequestParam(name = "supplierIds", required = false) List<Integer> supplierIds,
            @RequestParam(name = "secondaryCostCenters", required = false) List<String> secondaryCostCenterIds,
            @RequestParam(name = "lastUpdatedTimeAfter", required = false) OffsetDateTime lastUpdatedTimeAfter,
            @RequestParam(name = "lastUpdatedTimeBefore", required = false) OffsetDateTime lastUpdatedTimeBefore,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.ASC) Pageable pageable
            ) {
        return orderService.getAllOrders(
                primaryCostCenterIds,
                bookingYears,
                createdAfter,
                createdBefore,
                ownerIds,
                statuses,
                quotePriceMin,
                quotePriceMax,
                deliveryPersonIds,
                invoicePersonIds,
                queriesPersonIds,
                customerIds,
                supplierIds,
                secondaryCostCenterIds,
                lastUpdatedTimeAfter,
                lastUpdatedTimeBefore,
                pageable
        );
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        return orderService.createOrder(orderRequestDTO);
    }

    @GetMapping("{order-id}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable("order-id") Long id) {
        if(!orderService.existsOrderById(id)) throw new NotFoundException("Bestellung nicht gefunden.");
        return orderService.getOrderById(id);
    }

    @PatchMapping("{order-id}")
    public ResponseEntity<OrderResponseDTO> updateOrder(
            @PathVariable("order-id") Long id,
            @RequestBody OrderRequestDTO dto
    ){
        if(!orderService.existsOrderById(id)) throw new NotFoundException("Bestellung nicht gefunden.");
        if(!orderService.isOrderStatusEqual(id, OrderStatus.IN_PROGRESS)) throw new BadRequestException("Bestellstatus befindet sich nicht in Bearbeitung!");
        return orderService.updateOrder(dto, id);
    }

    @DeleteMapping("{order-id}")
    public ResponseEntity<String> deleteOrder(@PathVariable("order-id") Long id) {
        if(!orderService.existsOrderById(id)) throw new NotFoundException("Bestellung nicht gefunden.");
        return orderService.deleteOrderById(id);
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
        if(!orderService.isOrderStatusEqual(id, OrderStatus.IN_PROGRESS)) throw new BadRequestException("Bestellstatus befindet sich nicht in Bearbeitung!");
        return itemService.createItemsOfOrder(id, dtos);
    }

    @DeleteMapping("{order-id}/items/{item-id}")
    public ResponseEntity<String> deleteItemsOfOrder(
            @PathVariable("order-id") Long orderId,
            @PathVariable("item-id") Integer itemId
    ){
        if(!orderService.existsOrderById(orderId)) throw new NotFoundException("Bestellung nicht gefunden.");
        if(!itemService.existsItemOfOrder(orderId, itemId)) throw new NotFoundException("Artikel nicht gefunden.");
        if(!orderService.isOrderStatusEqual(orderId, OrderStatus.IN_PROGRESS)) throw new BadRequestException("Bestellstatus befindet sich nicht in Bearbeitung!");
        return itemService.deleteItemsOfOrder(orderId, itemId);
    }

    @GetMapping("{order-id}/quotations")
    public ResponseEntity<List<QuotationResponseDTO>> getQuotationsOfOrder(@PathVariable("order-id") Long id) {
        if(!orderService.existsOrderById(id)) throw new NotFoundException("Bestellung nicht gefunden.");
        return quotationService.getQuotationsOfOrder(id);
    }

    @DeleteMapping("{order-id}/quotations/{quotation-id}")
    public ResponseEntity<String> deleteQuotationsOfOrder(
            @PathVariable("order-id") Long orderId,
            @PathVariable("quotation-id") Short quotationId
    ){
        if(!orderService.existsOrderById(orderId)) throw new NotFoundException("Bestellung nicht gefunden.");
        if(!quotationService.existsQuotation(orderId, quotationId)) throw new NotFoundException("Vergleichsartikel nicht gefunden.");
        if(!orderService.isOrderStatusEqual(orderId, OrderStatus.IN_PROGRESS)) throw new BadRequestException("Bestellstatus befindet sich nicht in Bearbeitung!");
        return quotationService.deleteQuotation(orderId, quotationId);
    }

    @PostMapping("{order-id}/quotations")
    public ResponseEntity<List<QuotationResponseDTO>> createQuotationsOfOrder(
            @PathVariable("order-id") Long id,
            @RequestBody List<QuotationRequestDTO> dtos
    ){
        if(!orderService.existsOrderById(id)) throw new NotFoundException("Bestellung nicht gefunden.");
        if(!orderService.isOrderStatusEqual(id, OrderStatus.IN_PROGRESS)) throw new BadRequestException("Bestellstatus befindet sich nicht in Bearbeitung!");
        return quotationService.createQuotation(id, dtos);
    }

    @GetMapping("{order-id}/approvals")
    public ResponseEntity<ApprovalResponseDTO> getApprovalOfOrder(@PathVariable("order-id") Long orderId){
        if(!orderService.existsOrderById(orderId)) throw new NotFoundException("Bestellung nicht gefunden.");
        return this.approvalService.getApprovalOfOrder(orderId);
    }

    @PatchMapping("{order-id}/approvals")
    public ResponseEntity<ApprovalResponseDTO> updateApprovalOfOrder(
            @PathVariable("order-id") Long orderId,
            @RequestBody ApprovalRequestDTO dto
    ){
        if(!orderService.existsOrderById(orderId)) throw new NotFoundException("Bestellung nicht gefunden.");
        if(!orderService.isOrderStatusEqual(orderId, OrderStatus.COMPLETED)) throw new BadRequestException("Bestellstatus befindet sich nicht auf fertiggestellt!");
        return this.approvalService.updateApprovalOfOrder(orderId, dto);
    }

    @PutMapping("{order-id}/status")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable("order-id") Long orderId,
            @RequestBody OrderStatus orderStatus
    ){
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @RequestMapping("{order-id}/export")
    public ResponseEntity<byte[]> exportOrder(@PathVariable("order-id") Integer orderId) throws IOException {
        return this.orderPDFService.generateOrderPDF(orderId);
    }


}
