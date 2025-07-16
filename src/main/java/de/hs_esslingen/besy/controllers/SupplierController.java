package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.request.SupplierPOSTRequestDTO;
import de.hs_esslingen.besy.dtos.request.SupplierPUTRequestDTO;
import de.hs_esslingen.besy.dtos.response.SupplierResponseDTO;
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

    @GetMapping
    public ResponseEntity<List<SupplierResponseDTO>> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @PostMapping
    public ResponseEntity<SupplierResponseDTO> createSupplier(@RequestBody SupplierPOSTRequestDTO supplierPOSTRequestDTO) {
        return supplierService.createSupplier(supplierPOSTRequestDTO);
    }

    @PutMapping("{supplier-name}")
    public ResponseEntity<SupplierResponseDTO> updateSupplier(
            @PathVariable("supplier-name") String supplierName,
            @RequestBody SupplierPUTRequestDTO supplierPOSTRequestDTO) {
        return supplierService.updateSupplier(supplierName, supplierPOSTRequestDTO);
    }
}
