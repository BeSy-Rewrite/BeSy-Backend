package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.request.CustomerIdRequestDTO;
import de.hs_esslingen.besy.dtos.request.SupplierRequestDTO;
import de.hs_esslingen.besy.dtos.response.CustomerIdResponseDTO;
import de.hs_esslingen.besy.dtos.response.SupplierResponseDTO;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.services.CustomerIdService;
import de.hs_esslingen.besy.services.SupplierService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/suppliers")
public class SupplierController {

    private final SupplierService supplierService;
    private final CustomerIdService customerIdService;

    @GetMapping
    public ResponseEntity<List<SupplierResponseDTO>> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @GetMapping("/{supplier-id}")
    public ResponseEntity<SupplierResponseDTO> getSupplierById(@PathVariable("supplier-id") Integer id) {
        return supplierService.getSupplierById(id);
    }

    @PostMapping
    public ResponseEntity<SupplierResponseDTO> createSupplier(@RequestBody SupplierRequestDTO supplierRequestDTO) {
        return supplierService.createSupplier(supplierRequestDTO);
    }

    @GetMapping("/{supplier-id}/customer_ids")
    public ResponseEntity<List<CustomerIdResponseDTO>> getAllCustomerIds(@PathVariable("supplier-id") Integer id) {
        if(!supplierService.existsSupplierById(id)) throw new NotFoundException("Lieferant nicht gefunden.");
        return customerIdService.getAllCustomerIds(id);
    }

    @PostMapping("/{supplier-id}/customer_ids")
    public ResponseEntity<CustomerIdResponseDTO> createCustomerId(@PathVariable("supplier-id") Integer id, @RequestBody CustomerIdRequestDTO dto) {
        return customerIdService.createCustomerId(id, dto);
    }

}
