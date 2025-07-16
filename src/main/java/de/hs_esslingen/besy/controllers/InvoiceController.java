package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.response.InvoiceResponseDTO;
import de.hs_esslingen.besy.services.InvoiceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @RequestMapping("/order/{order-id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceOfOrder(@PathVariable("order-id") long orderId) {
        return invoiceService.getInvoiceOfOrder(orderId);
    }
}
