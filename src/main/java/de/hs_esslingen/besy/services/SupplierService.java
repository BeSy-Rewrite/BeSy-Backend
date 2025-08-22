package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.SupplierRequestDTO;
import de.hs_esslingen.besy.dtos.response.CreateSupplierResponseDTO;
import de.hs_esslingen.besy.dtos.response.SupplierResponseDTO;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.request.SupplierRequestMapper;
import de.hs_esslingen.besy.mappers.response.CreateSupplierResponseMapper;
import de.hs_esslingen.besy.mappers.response.SupplierResponseMapper;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.repositories.SupplierRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierRequestMapper supplierRequestMapper;
    private final SupplierResponseMapper supplierResponseMapper;
    private final CreateSupplierResponseMapper createSupplierResponseMapper;

    public ResponseEntity<List<SupplierResponseDTO>> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAll();
        List<SupplierResponseDTO> supplierResponseDTOS = supplierResponseMapper.toDto(suppliers);
        return ResponseEntity.ok(supplierResponseDTOS);
    }

    public ResponseEntity<SupplierResponseDTO> getSupplierById(Integer id) {
        return supplierRepository.findById(id)
                .map(supplier -> {
                    return ResponseEntity.ok(supplierResponseMapper.toDto(supplier));
                }).orElseThrow(() -> new NotFoundException("Lieferant mit id " + id + " nicht gefunden."));
    }

    public ResponseEntity<CreateSupplierResponseDTO> createSupplier(SupplierRequestDTO supplierRequestDTO) {
        Supplier supplier = supplierRequestMapper.toEntity(supplierRequestDTO);
        Supplier supplierPersisted = supplierRepository.save(supplier);
        return ResponseEntity.ok(createSupplierResponseMapper.toDto(supplierPersisted));
    }

    public boolean existsSupplierById(Integer id) {
        return supplierRepository.existsById(id);
    }

}
