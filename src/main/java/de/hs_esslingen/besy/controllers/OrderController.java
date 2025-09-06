package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.request.ItemRequestDTO;
import de.hs_esslingen.besy.dtos.request.OrderRequestDTO;
import de.hs_esslingen.besy.dtos.request.QuotationRequestDTO;
import de.hs_esslingen.besy.dtos.response.InvoiceResponseDTO;
import de.hs_esslingen.besy.dtos.response.ItemResponseDTO;
import de.hs_esslingen.besy.dtos.response.OrderResponseDTO;
import de.hs_esslingen.besy.dtos.response.QuotationResponseDTO;
import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.repositories.InvoiceRepository;
import de.hs_esslingen.besy.services.*;
import lombok.AllArgsConstructor;
import org.apache.hc.core5.http.ParseException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final PaperlessService paperlessService;
    private final InvoiceRepository invoiceRepository;

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
        return itemService.createItemsOfOrder(id, dtos);
    }

    @DeleteMapping("{order-id}/items/{item-id}")
    public ResponseEntity<String> deleteItemsOfOrder(
            @PathVariable("order-id") Long orderId,
            @PathVariable("item-id") Integer itemId
    ){
        if(!orderService.existsOrderById(orderId)) throw new NotFoundException("Bestellung nicht gefunden.");
        if(!itemService.existsItemOfOrder(orderId, itemId)) throw new NotFoundException("Artikel nicht gefunden.");
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
        return quotationService.deleteQuotation(orderId, quotationId);
    }

    @PostMapping("{order-id}/quotations")
    public ResponseEntity<List<QuotationResponseDTO>> createQuotationsOfOrder(
            @PathVariable("order-id") Long id,
            @RequestBody List<QuotationRequestDTO> dtos
    ){
        if(!orderService.existsOrderById(id)) throw new NotFoundException("Bestellung nicht gefunden.");
        return quotationService.createQuotation(id, dtos);
    }

    @PostMapping("invoice/{invoice-id}/document")
    public ResponseEntity<InvoiceResponseDTO> createInvoiceOfOrder(
            @RequestParam("file") MultipartFile file,
            @PathVariable("invoice-id") String invoiceId
    ) throws IOException, ParseException {
        if(invoiceRepository.existsById(invoiceId)) throw new NotFoundException("Rechnung nicht gefunden.");
        return paperlessService.uploadPdfToPaperless(file, invoiceId);
    }

    @GetMapping
    @RequestMapping("{order-id}/export")
    public ResponseEntity<byte[]> exportOrder(@PathVariable("order-id") Integer orderId) throws IOException {
        return this.orderPDFService.generateOrderPDF(orderId);
    }


}
