package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.response.CustomerIdRequestDTO;
import de.hs_esslingen.besy.dtos.response.CustomerIdResponseDTO;
import de.hs_esslingen.besy.services.CustomerIdService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<CustomerIdResponseDTO> createCustomerId(@RequestBody CustomerIdRequestDTO customerIdRequestDTO) {
        return customerIdService.createCustomerId(customerIdRequestDTO);
    }
}
