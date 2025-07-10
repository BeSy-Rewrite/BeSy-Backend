package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.dto.CustomerIdResponseDTO;
import de.hs_esslingen.besy.service.CustomerIdService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/customer_id")
public class CustomerIdController {

    private final CustomerIdService customerIdService;

    @GetMapping
    public ResponseEntity<List<CustomerIdResponseDTO>> getAllCustomerIds() {
        return customerIdService.getAllCustomerIds();
    }
}
