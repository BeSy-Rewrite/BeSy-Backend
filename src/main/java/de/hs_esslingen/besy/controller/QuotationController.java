package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.dto.QuotationResponseDTO;
import de.hs_esslingen.besy.service.QuotationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/quotations")
public class QuotationController {

    private final QuotationService quotationService;

    @GetMapping
    @RequestMapping("/{order-id}")
    public ResponseEntity<List<QuotationResponseDTO>> getQuotationsOfOrder(@PathVariable("order-id") long orderId) {
        return quotationService.getQuotationsOfOrder(orderId);
    }
}
