package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.dto.request.SupplierRequestDTO;
import de.hs_esslingen.besy.dto.response.SupplierResponseDTO;
import de.hs_esslingen.besy.service.SupplierService;
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
    public ResponseEntity<SupplierResponseDTO> createSupplier(@RequestBody SupplierRequestDTO supplierRequestDTO) {
        return supplierService.createSupplier(supplierRequestDTO);
    }

    @PutMapping("{supplier-name}")
    public ResponseEntity<SupplierResponseDTO> updateSupplier(
            @PathVariable("supplier-name") String supplierName,
            @RequestBody SupplierRequestDTO supplierRequestDTO) {
        return supplierService.updateSupplier(supplierName, supplierRequestDTO);
    }
}
