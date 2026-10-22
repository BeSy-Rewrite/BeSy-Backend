package de.hs_esslingen.besy.controllers;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.hs_esslingen.besy.dtos.request.ApprovalRequestDTO;
import de.hs_esslingen.besy.dtos.request.InvoiceRequestDTO;
import de.hs_esslingen.besy.dtos.request.ItemRequestDTO;
import de.hs_esslingen.besy.dtos.request.OrderRequestDTO;
import de.hs_esslingen.besy.dtos.request.QuotationRequestDTO;
import de.hs_esslingen.besy.dtos.response.ApprovalResponseDTO;
import de.hs_esslingen.besy.dtos.response.InvoiceResponseDTO;
import de.hs_esslingen.besy.dtos.response.ItemResponseDTO;
import de.hs_esslingen.besy.dtos.response.OrderResponseDTO;
import de.hs_esslingen.besy.dtos.response.OrderStatusHistoryResponseDTO;
import de.hs_esslingen.besy.dtos.response.QuotationResponseDTO;
import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.exceptions.BadRequestException;
import de.hs_esslingen.besy.exceptions.EntityAlreadyExistsException;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.response.OrderResponseMapper;
import de.hs_esslingen.besy.mappers.response.QuotationResponseMapper;
import de.hs_esslingen.besy.models.Invoice;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.pdf.OrderPDFService;
import de.hs_esslingen.besy.services.ApprovalService;
import de.hs_esslingen.besy.services.CostCenterService;
import de.hs_esslingen.besy.services.InvoiceService;
import de.hs_esslingen.besy.services.ItemService;
import de.hs_esslingen.besy.services.OrderService;
import de.hs_esslingen.besy.services.PaperlessService;
import de.hs_esslingen.besy.services.QuotationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(value = "${api.prefix}/orders", produces = "application/json")
public class OrderController {
    private final OrderService orderService;
    private final ItemService itemService;
    private final QuotationService quotationService;
    private final OrderPDFService orderPDFService;
    private final PaperlessService paperlessService;
    private final InvoiceService invoiceService;
    private final ApprovalService approvalService;
    private final CostCenterService costCenterService;
    private final OrderResponseMapper orderResponseMapper;
    private final QuotationResponseMapper quotationResponseMapper;

    @GetMapping
    @Operation(description = "Gibt Bestellungen paginiert zurück und ermöglicht die Filterung nach Kostenstellen, Personen, Status, Preisen, Lieferanten, Zeitpunkten und Suchbegriffen.", parameters = {
            @Parameter(name = "page", in = ParameterIn.QUERY, required = false, schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", in = ParameterIn.QUERY, required = false, schema = @Schema(type = "integer", defaultValue = "10")),
            @Parameter(name = "sort", in = ParameterIn.QUERY, required = false, array = @ArraySchema(schema = @Schema(type = "string", example = "bookingYear,desc")), description = "Sortierung der Ergebnisse. Standardmäßig nach Erstellungsdatum absteigend sortiert.\nUnterstützt mehrfache Sortierungen. Erster Parameter ist die Hauptsortierung, der letzte die niedrigste Sortierung. Anders ausgedrückt: Die Sortierungen werden von rechts nach links angewendet.")
    })
    @ApiResponse(responseCode = "200", description = "Liste der Bestellungen mit Paginierungsinformationen.")
    public Page<OrderResponseDTO> getAllOrders(
            @Parameter(description = "Filtert nach IDs der primären Kostenstellen.") @RequestParam(name = "primaryCostCenters", required = false) List<String> primaryCostCenterIds,
            @Parameter(description = "Filtert nach den letzten zwei Ziffern der Jahreszahl der Buchung. Diese muss ein String sein, z. B. \"25\".") @RequestParam(name = "bookingYears", required = false) List<String> bookingYears,
            @Parameter(description = "Filtert nach Bestellungen, welche nach oder zu diesem Zeitpunkt erstellt wurden.", examples = {
                    @ExampleObject(value = "2023-01-01T00:00:00Z"),
                    @ExampleObject(value = "2023-01-01T00:00:00+01:00") }) @RequestParam(name = "createdAfter", required = false) OffsetDateTime createdAfter,
            @Parameter(description = "Filtert nach Bestellungen, welche vor oder zu diesem Zeitpunkt erstellt wurden.", examples = {
                    @ExampleObject(value = "2023-01-01T00:00:00Z"),
                    @ExampleObject(value = "2023-01-01T00:00:00+01:00") }) @RequestParam(name = "createdBefore", required = false) OffsetDateTime createdBefore,
            @Parameter(description = "Filtert nach IDs der Ersteller der Bestellung.") @RequestParam(name = "ownerIds", required = false) List<Integer> ownerIds,
            @Parameter(description = "Filtert nach dem Bestellstatus. Beinhaltet defaultmäßig alle Bestellstatus.") @RequestParam(name = "statuses", required = false) List<OrderStatus> statuses,
            @Parameter(description = "Filtert nach quotePriceMin.") @RequestParam(name = "quotePriceMin", required = false) BigDecimal quotePriceMin,
            @Parameter(description = "Filtert nach quotePriceMax.") @RequestParam(name = "quotePriceMax", required = false) BigDecimal quotePriceMax,
            @Parameter(description = "Filtert nach IDs der Besteller.") @RequestParam(name = "deliveryPersonIds", required = false) List<Long> deliveryPersonIds,
            @Parameter(description = "Filtert nach IDs der Rechnungsempfänger.") @RequestParam(name = "invoicePersonIds", required = false) List<Long> invoicePersonIds,
            @Parameter(description = "Filtert nach IDs der anfragenden Personen.") @RequestParam(name = "queriesPersonIds", required = false) List<Long> queriesPersonIds,
            @Parameter(description = "Filtert nach Kundennummern.") @RequestParam(name = "customerIds", required = false) List<String> customerIds,
            @Parameter(description = "Filtert nach IDs der Lieferanten.") @RequestParam(name = "supplierIds", required = false) List<Integer> supplierIds,
            @Parameter(description = "Filtert nach IDs der sekundären Kostenstellen.") @RequestParam(name = "secondaryCostCenters", required = false) List<String> secondaryCostCenterIds,
            @Parameter(description = "Filtert nach Bestellungen, welche nach oder zu diesem Zeitpunkt bearbeitet wurden.", examples = {
                    @ExampleObject(value = "2023-01-01T00:00:00Z"),
                    @ExampleObject(value = "2023-01-01T00:00:00+01:00") }) @RequestParam(name = "lastUpdatedTimeAfter", required = false) OffsetDateTime lastUpdatedTimeAfter,
            @Parameter(description = "Filtert nach Bestellungen, welche vor oder zu diesem Zeitpunkt bearbeitet wurden.", examples = {
                    @ExampleObject(value = "2023-01-01T00:00:00Z"),
                    @ExampleObject(value = "2023-01-01T00:00:00+01:00") }) @RequestParam(name = "lastUpdatedTimeBefore", required = false) OffsetDateTime lastUpdatedTimeBefore,
            @Parameter(description = "Filtert nach Bestellnummern, die größer oder gleich diesem Wert sind.") @RequestParam(name = "autoIndexGTE", required = false) Short autoIndexGTE,
            @Parameter(description = "Filtert nach Bestellnummern, die kleiner oder gleich diesem Wert sind.") @RequestParam(name = "autoIndexLTE", required = false) Short autoIndexLTE,
            @Parameter(description = "Filtert nach Bestellungen, die den Suchbegriff enthalten.") @RequestParam(name = "searchTerm", required = false) String searchTerm,
            @Parameter(hidden = true) @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
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
                autoIndexGTE,
                autoIndexLTE,
                searchTerm,
                pageable,
                jwt);
    }

    @PostMapping
    @Operation(description = "Erstellt eine neue Bestellung und setzt den Bestellstatus (`status`) auf \"In Bearbeitung\" (`IN_PROGRESS`). Nur in diesem Bestellstatus ist das Bearbeiten einer Bestellung möglich.")
    @ApiResponse(responseCode = "200", description = "Bestellung erfolgreich erstellt.")
    @ApiResponse(responseCode = "409", description = "Verweis auf eine nicht existierende Entität.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO orderRequestDTO,
            @AuthenticationPrincipal Jwt jwt) {
        Order createdOrder = orderService.createOrder(orderRequestDTO, jwt);
        OrderResponseDTO dto = orderResponseMapper.toDto(createdOrder);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("{order-id}")
    @Operation(description = "Gibt eine Bestellung anhand ihrer eindeutigen ID zurück.")
    @ApiResponse(responseCode = "200", description = "Bestellung erfolgreich abgerufen.")
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden.")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @Parameter(description = "Die eindeutige ID der Bestellung, die abgerufen werden soll.") @PathVariable("order-id") Long id) {
        if (!orderService.existsOrderById(id))
            throw new NotFoundException("Bestellung nicht gefunden.");

        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new NotFoundException("Bestellung mit id " + id + " nicht gefunden."));
        OrderResponseDTO dto = orderResponseMapper.toDto(order);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("{order-id}")
    @Operation(description = "Aktualisiert eine Bestellung. Nur nicht-leere Felder im Request-Body werden übernommen. Nur möglich, wenn sich die Bestellung im Bestellstatus (`status`) \"In Bearbeitung\" (`IN_PROGRESS`) befindet.")
    @ApiResponse(responseCode = "200", description = "Bestellung erfolgreich aktualisiert.")
    @ApiResponse(responseCode = "400", description = "Bestellstatus befindet sich nicht in Bearbeitung (`IN_PROGRESS`)!")
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden.")
    @ApiResponse(responseCode = "409", description = "Verweis auf eine nicht existierende Entität.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<OrderResponseDTO> updateOrder(
            @Parameter(description = "Die eindeutige ID der Bestellung, die verändert werden soll.") @PathVariable("order-id") Long id,
            @RequestBody OrderRequestDTO dto) {
        if (!orderService.existsOrderById(id))
            throw new NotFoundException("Bestellung nicht gefunden.");
        if (!orderService.isOrderStatusEqual(id, OrderStatus.IN_PROGRESS) && hasNonCommentUpdates(dto))
            throw new BadRequestException("Bestellstatus befindet sich nicht in Bearbeitung!");
        return ResponseEntity.ok(orderResponseMapper.toDto(orderService.updateOrder(dto, id)));
    }

    private boolean hasNonCommentUpdates(OrderRequestDTO dto) {
        if (dto == null)
            return false;

        BeanWrapper beanWrapper = new BeanWrapperImpl(dto);
        for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            if ("class".equals(propertyName) || "comment".equals(propertyName))
                continue;

            if (beanWrapper.getPropertyValue(propertyName) != null)
                return true;
        }

        return false;
    }

    @DeleteMapping("{order-id}")
    @Operation(description = "Löscht eine bestehende Bestellung anhand der `order-id` (Soft Delete). Setzt den Bestellstatus (`order_status`) auf gelöscht (`DELETED`).")
    @ApiResponse(responseCode = "204", description = "Bestellung erfolgreich gelöscht (No Content).")
    @ApiResponse(responseCode = "400", description = "Bestellstatus befindet sich nicht in gültigem Bestellstatus!")
    @ApiResponse(responseCode = "404", description = "Bestellung mit der angegebenen ID nicht gefunden.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<String> deleteOrder(
            @Parameter(description = "ID der zu löschenden Bestellung.") @PathVariable("order-id") Long id) {
        if (!orderService.existsOrderById(id))
            throw new NotFoundException("Bestellung nicht gefunden.");
        if (!orderService.isOrderStatusEqual(id, orderService.getStatusesAllowingTransitionTo(OrderStatus.DELETED)))
            throw new BadRequestException("Bestellstatus befindet sich nicht in gültigem Bestellstatus!");
        return orderService.deleteOrderById(id);
    }

    @GetMapping("{order-id}/items")
    @Operation(description = "Gibt die Artikel der angegebenen Bestellung zurück.")
    @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufene Artikel einer Bestellung.")
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden.")
    public ResponseEntity<List<ItemResponseDTO>> getOrderItems(
            @Parameter(description = "Die eindeutige ID der Bestellung, für welche die Artikel abgerufen werden sollen.") @PathVariable("order-id") Long id) {
        if (!orderService.existsOrderById(id))
            throw new NotFoundException("Bestellung nicht gefunden.");
        return itemService.getItemsOfOrder(id);
    }

    @PostMapping("{order-id}/items")
    @Operation(description = "Erstellt neue Artikel einer Bestellung.")
    @ApiResponse(responseCode = "201", description = "Artikel erfolgreich erstellt.")
    @ApiResponse(responseCode = "400", description = "Bestellstatus befindet sich nicht in Bearbeitung (`IN_PROGRESS`)!")
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden.")
    @ApiResponse(responseCode = "409", description = "Zugehöriger Artikel mit Artikelnummer `article_id` existiert bereits.")
    public ResponseEntity<List<ItemResponseDTO>> createOrderItem(
            @Parameter(description = "Die eindeutige ID der Bestellung, für welche neue Artikel angelegt werden sollen.") @PathVariable("order-id") Long id,
            @RequestBody List<ItemRequestDTO> dtos) {
        if (!orderService.existsOrderById(id))
            throw new NotFoundException("Bestellung nicht gefunden.");
        if (!orderService.isOrderStatusEqual(id, OrderStatus.IN_PROGRESS))
            throw new BadRequestException("Bestellstatus befindet sich nicht in Bearbeitung!");
        return itemService.createItemsOfOrder(id, dtos);
    }

    @PatchMapping("{order-id}/items/{item-id}")
    @Operation(description = "Aktualisiert einen Artikel einer bestehenden Bestellung.")
    @ApiResponse(responseCode = "200", description = "Artikel erfolgreich aktualisiert.")
    @ApiResponse(responseCode = "400", description = "Bestellstatus befindet sich nicht in Bearbeitung (`IN_PROGRESS`)!")
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden. Artikel nicht gefunden.")
    public ResponseEntity<ItemResponseDTO> updateOrderItem(
            @Parameter(description = "ID der Bestellung, deren Artikel aktualisiert werden soll.") @PathVariable("order-id") Long orderId,
            @Parameter(description = "ID des zu aktualisierenden Artikels.") @PathVariable("item-id") Integer itemId,
            @RequestBody ItemRequestDTO dto) {
        if (!orderService.existsOrderById(orderId))
            throw new NotFoundException("Bestellung nicht gefunden.");
        if (!itemService.existsItemOfOrder(orderId, itemId))
            throw new NotFoundException("Artikel nicht gefunden.");
        if (!orderService.isOrderStatusEqual(orderId, OrderStatus.IN_PROGRESS))
            throw new BadRequestException("Bestellstatus befindet sich nicht in Bearbeitung!");
        return itemService.updateItemOfOrder(orderId, itemId, dto);
    }

    @DeleteMapping("{order-id}/items/{item-id}")
    @Operation(description = "Löscht einen Artikel einer bestehenden Bestellung (`order-id`) anhand der `item-id` (Hard Delete).")
    @ApiResponse(responseCode = "204", description = "Bestellung erfolgreich gelöscht (No Content).")
    @ApiResponse(responseCode = "400", description = "Bestellstatus befindet sich nicht in Bearbeitung (`IN_PROGRESS`)!")
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden. Artikel nicht gefunden.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<String> deleteOrderItem(
            @Parameter(description = "ID der Bestellung, deren Artikel gelöscht werden soll.") @PathVariable("order-id") Long orderId,
            @Parameter(description = "ID des Artikels, der gelöscht werden soll.") @PathVariable("item-id") Integer itemId) {
        if (!orderService.existsOrderById(orderId))
            throw new NotFoundException("Bestellung nicht gefunden.");
        if (!itemService.existsItemOfOrder(orderId, itemId))
            return ResponseEntity.noContent().build();
        if (!orderService.isOrderStatusEqual(orderId, OrderStatus.IN_PROGRESS))
            throw new BadRequestException("Bestellstatus befindet sich nicht in Bearbeitung!");
        return itemService.deleteItemOfOrder(orderId, itemId);
    }

    @GetMapping("{order-id}/invoices")
    @Operation(description = "Gibt die Rechnungen der angegebenen Bestellung zurück.")
    @ApiResponse(responseCode = "200", description = "Liste der zugehörigen Rechnungen")
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<List<InvoiceResponseDTO>> getOrderInvoices(
            @Parameter(description = "ID der Bestellung") @PathVariable("order-id") Long orderId) {
        if (!orderService.existsOrderById(orderId))
            throw new NotFoundException("Bestellung nicht gefunden.");
        return invoiceService.getAllInvoices(orderId);
    }

    @PostMapping("{order-id}/invoices")
    @Operation(description = "Erstellt eine Rechnung für die angegebene Bestellung.")
    @ApiResponse(responseCode = "200", description = "Rechnung erfolgreich erstellt")
    @ApiResponse(responseCode = "400", description = "Ungültige Anfrage (z. B. falscher Bestellstatus).")
    @ApiResponse(responseCode = "404", description = "Bestellung oder Kostenstelle nicht gefunden.")
    @ApiResponse(responseCode = "409", description = "Rechnung existiert bereits.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<InvoiceResponseDTO> createOrderInvoice(
            @Parameter(description = "ID der Bestellung") @PathVariable("order-id") Long orderId,
            @RequestBody InvoiceRequestDTO dto) {
        if (!orderService.existsOrderById(orderId))
            throw new NotFoundException("Bestellung nicht gefunden.");
        if (invoiceService.existsInvoiceById(dto.getId()))
            throw new EntityAlreadyExistsException("Rechnung existiert bereits.");
        if (!costCenterService.existsById(dto.getCostCenterId()))
            throw new NotFoundException("Kostenstelle nicht gefunden.");
        return this.invoiceService.createInvoice(dto, orderId);
    }

    @GetMapping("{order-id}/quotations")
    @Operation(description = "Gibt die Vergleichsangebote der angegebenen Bestellung zurück.")
    @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufene Vergleichsangebote einer Bestellung.")
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden.")
    public ResponseEntity<List<QuotationResponseDTO>> getOrderQuotations(
            @Parameter(description = "Die eindeutige ID der Bestellung, für welche die Vergleichsangebote abgerufen werden sollen.") @PathVariable("order-id") Long id) {
        if (!orderService.existsOrderById(id))
            throw new NotFoundException("Bestellung nicht gefunden.");
        return quotationService.getQuotationsOfOrder(id);
    }

    @DeleteMapping("{order-id}/quotations/{quotation-id}")
    @Operation(description = "Löscht ein Vergleichsangebot einer bestehenden Bestellung (`order-id`) anhand der `quotation-id` (Hard Delete).")
    @ApiResponse(responseCode = "204", description = "Vergleichsartikel erfolgreich gelöscht (No Content).")
    @ApiResponse(responseCode = "400", description = "Bestellstatus befindet sich nicht in Bearbeitung (`IN_PROGRESS`)!")
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden. Vergleichsartikel nicht gefunden.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<String> deleteOrderQuotation(
            @Parameter(description = "ID der Bestellung, deren Vergleichsartikel gelöscht werden soll.") @PathVariable("order-id") Long orderId,
            @Parameter(description = "ID des Vergleichsartikels, der gelöscht werden soll.") @PathVariable("quotation-id") Short quotationId) {
        if (!orderService.existsOrderById(orderId))
            throw new NotFoundException("Bestellung nicht gefunden.");
        if (!quotationService.existsQuotation(orderId, quotationId))
            return ResponseEntity.noContent().build();
        if (!orderService.isOrderStatusEqual(orderId, OrderStatus.IN_PROGRESS))
            throw new BadRequestException("Bestellstatus befindet sich nicht in Bearbeitung!");
        return quotationService.deleteQuotation(orderId, quotationId);
    }

    @PostMapping("{order-id}/quotations")
    @Operation(description = "Erstellt neue Vergleichsangebote einer Bestellung.")
    @ApiResponse(responseCode = "201", description = "Vergleichsartikel erfolgreich erstellt.")
    @ApiResponse(responseCode = "400", description = "Bestellstatus befindet sich nicht in Bearbeitung (`IN_PROGRESS`)!")
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden.")
    @ApiResponse(responseCode = "409", description = "Zugehöriges Vergleichsangebot mit dem Index `index` existiert bereits.")
    public ResponseEntity<List<QuotationResponseDTO>> createOrderQuotation(
            @Parameter(description = "Die eindeutige ID der Bestellung, für welche neue Vergleichsangebote angelegt werden sollen.") @PathVariable("order-id") Long id,
            @RequestBody List<QuotationRequestDTO> dtos) {
        if (!orderService.existsOrderById(id))
            throw new NotFoundException("Bestellung nicht gefunden.");
        if (!orderService.isOrderStatusEqual(id, OrderStatus.IN_PROGRESS))
            throw new BadRequestException("Bestellstatus befindet sich nicht in Bearbeitung!");
        return quotationService.createQuotation(id, dtos);
    }

    @PatchMapping("{order-id}/quotations/{quotation-id}")
    @Operation(description = "Aktualisiert ein vorhandenes Vergleichsangebot einer bestehenden Bestellung.")
    @ApiResponse(responseCode = "200", description = "Vergleichsangebot erfolgreich aktualisiert.")
    @ApiResponse(responseCode = "400", description = "Bestellstatus befindet sich nicht in Bearbeitung (`IN_PROGRESS`)!")
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden. Angebot nicht gefunden.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<QuotationResponseDTO> updateOrderQuotation(
            @Parameter(description = "ID der Bestellung, deren Angebot aktualisiert werden soll.") @PathVariable("order-id") Long orderId,
            @Parameter(description = "ID des zu aktualisierenden Angebots.") @PathVariable("quotation-id") Short index,
            @RequestBody QuotationRequestDTO dto) {
        if (!orderService.existsOrderById(orderId))
            throw new NotFoundException("Bestellung nicht gefunden.");
        if (!quotationService.existsQuotation(orderId, index))
            throw new NotFoundException("Angebot nicht gefunden.");
        if (!orderService.isOrderStatusEqual(orderId, OrderStatus.IN_PROGRESS))
            throw new BadRequestException("Bestellstatus befindet sich nicht in Bearbeitung!");
        return ResponseEntity.ok(quotationResponseMapper.toDto(quotationService.updateQuotation(orderId, index, dto)));
    }

    @GetMapping(value = "invoice/{invoice-id}/document", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(description = "Gibt das vollständige PDF-Dokument zur angegebenen Rechnung zurück.")
    @ApiResponse(responseCode = "200", description = "PDF erfolgreich zurückgegeben", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "404", description = "Rechnung nicht gefunden.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<byte[]> getPdfOfInvoice(
            @Parameter(description = "ID der Rechnung, für die das PDF heruntergeladen wird.") @PathVariable("invoice-id") String invoiceId)
            throws IOException {
        if (!invoiceService.existsInvoiceById(invoiceId))
            throw new NotFoundException("Rechnung nicht gefunden.");
        return paperlessService.getPdfOfInvoice(invoiceId);
    }

    @PostMapping(value = "invoice/{invoice-id}/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Lädt eine PDF-Datei hoch und verknüpft sie nach der Verarbeitung mit der angegebenen Rechnung. Jede Rechnung kann ausschließlich mit einem PDF-Dokument verknüpft werden.")
    @ApiResponse(responseCode = "200", description = "Dokument erfolgreich hochgeladen und mit der Rechnung verknüpft")
    @ApiResponse(responseCode = "400", description = "Diese Rechnung besitzt bereits ein verknüpftes Dokument mit der id `paperless_id`.")
    @ApiResponse(responseCode = "404", description = "Rechnung nicht gefunden.")
    @ApiResponse(responseCode = "500", description = "Fehler beim Hochladen des Dokumentes. Interner Serverfehler.")
    public ResponseEntity<InvoiceResponseDTO> createOrderInvoice(
            @Parameter(description = "Die hochzuladende PDF-Datei.", content = @Content(mediaType = "multipart/form-data")) @RequestParam("file") MultipartFile file,
            @Parameter(description = "Die ID der Rechnung, mit der das Dokument verknüpft werden soll") @PathVariable("invoice-id") String invoiceId)
            throws IOException, ParseException {
        if (!invoiceService.existsInvoiceById(invoiceId))
            throw new NotFoundException("Rechnung nicht gefunden.");
        return paperlessService.uploadPdfToPaperless(file, invoiceId);
    }

    @GetMapping(value = "invoice/{invoice-id}/document/preview", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(description = "Gibt eine Vorschau des PDF-Dokuments als IMG-Datei zur angegebenen Rechnung zurück.")
    @ApiResponse(responseCode = "200", description = "PDF-Vorschau erfolgreich zurückgegeben", content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE, schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "404", description = "Rechnung nicht gefunden.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<byte[]> getInvoicePdfPreview(
            @Parameter(description = "ID der Rechnung, für die die PDF-Vorschau abgerufen wird.") @PathVariable("invoice-id") String invoiceId)
            throws IOException {
        if (!invoiceService.existsInvoiceById(invoiceId))
            throw new NotFoundException("Rechnung nicht gefunden.");

        Invoice invoice = invoiceService.getInvoiceById(invoiceId);
        HttpEntity entity = paperlessService.getPreviewOfPdfOfInvoice(invoiceId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDisposition(ContentDisposition.builder(invoice.getId() + ".webp").build());

        return new ResponseEntity<>(EntityUtils.toByteArray(entity), headers, HttpStatus.OK);
    }

    @GetMapping("{order-id}/approvals")
    @Operation(description = "Gibt die Zustimmungen für die angegebene Bestellung zurück.")
    @ApiResponse(responseCode = "200", description = "Zustimmungen erfolgreich abgerufen.")
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<ApprovalResponseDTO> getOrderApprovals(
            @Parameter(description = "ID der Bestellung, deren Zustimmungen abgerufen werden.") @PathVariable("order-id") Long orderId) {
        if (!orderService.existsOrderById(orderId))
            throw new NotFoundException("Bestellung nicht gefunden.");
        return this.approvalService.getApprovalOfOrder(orderId);
    }

    @PatchMapping("{order-id}/approvals")
    @Operation(description = "Aktualisiert Felder der Zustimmungen für die angegebene Bestellung. Nur nicht-leere Felder im Request-Body werden übernommen.")
    @ApiResponse(responseCode = "200", description = "Approval successfully updated")
    @ApiResponse(responseCode = "400", description = "Bestellstatus befindet sich nicht auf fertiggestellt (`COMPLETED`)!")
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<ApprovalResponseDTO> updateOrderApprovals(
            @Parameter(description = "ID der Bestellung, deren Zustimmungen aktualisiert werden.") @PathVariable("order-id") Long orderId,
            @RequestBody ApprovalRequestDTO dto) {
        if (!orderService.existsOrderById(orderId))
            throw new NotFoundException("Bestellung nicht gefunden.");
        if (!orderService.isOrderStatusEqual(orderId, Set.of(OrderStatus.COMPLETED, OrderStatus.IN_PROGRESS)))
            throw new BadRequestException("Bestellstatus befindet sich nicht auf fertiggestellt!");
        return this.approvalService.updateApprovalOfOrder(orderId, dto);
    }

    @GetMapping("/statuses")
    @Operation(description = "Gibt eine Map zurück, die jedem OrderStatus die erlaubten nächsten OrderStatus-Werte zuordnet.")
    @ApiResponse(responseCode = "200", description = "Erfolgreiche Abfrage der Order-Status-Matrix.")
    public ResponseEntity<Map<OrderStatus, Set<OrderStatus>>> getOrderStates() {
        return ResponseEntity.ok(OrderService.getOrderStatusMatrix());
    }

    @PutMapping("{order-id}/status")
    @Operation(description = "Aktualisiert den Bestellstatus (`status`) mit der angegebenen `order-id`. Wirft einen Fehler, wenn der neue Status `DELETED` ist – bitte stattdessen den DELETE-Endpunkt verwenden.")
    @ApiResponse(responseCode = "200", description = "Bestellstatus erfolgreich aktualisiert.")
    @ApiResponse(responseCode = "400", description = "Löschen nicht erlaubt, nutze DELETE endpoint! Ungültiger Statusübergang von `old_status` zu `new_status`. primary_cost_center_id darf nicht null sein.")
    public ResponseEntity<OrderStatus> updateOrderState(
            @Parameter(description = "ID der Bestellung, deren Bestellstatus aktualisiert wird.") @PathVariable("order-id") Long orderId,
            @RequestBody OrderStatus targetOrderStatus,
            @AuthenticationPrincipal Jwt jwt) {
        if (targetOrderStatus.equals(OrderStatus.DELETED))
            throw new BadRequestException("Löschen nicht erlaubt, nutze DELETE endpoint!");
        return orderService.updateOrderStatus(orderId, targetOrderStatus, jwt);
    }

    @GetMapping("{order-id}/status/history")
    @Operation(description = "Gibt eine Liste des Verlaufes des Bestellstatus einer Bestellung zurück.")
    @ApiResponse(responseCode = "200", description = "Liste des Statusverlaufs erfolgreich abgerufen")
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden.")
    public ResponseEntity<List<OrderStatusHistoryResponseDTO>> getOrderStateHistory(
            @Parameter(description = "ID der Bestellung, deren Bestellstatus-Historie abgerufen werden soll.") @PathVariable("order-id") Long orderId) {
        if (!orderService.existsOrderById(orderId))
            throw new NotFoundException("Bestellung nicht gefunden.");
        return orderService.getStatusHistory(orderId);

    }

    @GetMapping(value = "{order-id}/export", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(description = "Exportiert das Bestellformular der angegebenen Bestellung als PDF.")
    @ApiResponse(responseCode = "200", description = "PDF-Formular der Bestellung erfolgreich abgerufen.", content = @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "404", description = "Bestellung nicht gefunden.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<byte[]> exportOrder(
            @Parameter(description = "Die eindeutige ID der Bestellung, für die das Bestellformular als PDF exportiert wird.") @PathVariable("order-id") Long orderId)
            throws IOException {
        if (!orderService.existsOrderById(orderId))
            throw new NotFoundException("Bestellung nicht gefunden.");
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(this.orderPDFService.generateOrderPDF(orderId));
    }

}
