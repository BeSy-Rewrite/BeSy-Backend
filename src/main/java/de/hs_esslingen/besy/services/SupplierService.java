package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.SupplierRequestDTO;
import de.hs_esslingen.besy.dtos.response.CreateSupplierResponseDTO;
import de.hs_esslingen.besy.dtos.response.CustomerIdResponseDTO;
import de.hs_esslingen.besy.dtos.response.SupplierResponseDTO;
import de.hs_esslingen.besy.enums.AddressOwnerType;
import de.hs_esslingen.besy.exceptions.BadRequestException;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.request.AddressRequestMapper;
import de.hs_esslingen.besy.mappers.request.SupplierRequestMapper;
import de.hs_esslingen.besy.mappers.response.CreateSupplierResponseMapper;
import de.hs_esslingen.besy.mappers.response.SupplierResponseMapper;
import de.hs_esslingen.besy.models.Address;
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
    private final AddressRequestMapper addressRequestMapper;

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
        if (supplier.getAddress() != null) {
            supplier.getAddress().setOwnerType(AddressOwnerType.Supplier);
        }
        Supplier supplierPersisted = supplierRepository.save(supplier);
        return ResponseEntity.ok(createSupplierResponseMapper.toDto(supplierPersisted));
    }

    public ResponseEntity<CreateSupplierResponseDTO> updateSupplier(Integer id, SupplierRequestDTO dto) {
        Supplier supplier = supplierRepository.findById(id).get();
        Address address = supplier.getAddress();

        supplierRequestMapper.partialUpdate(supplier, dto);

        if(supplier.getAddress() != null) {
            if (supplier.getAddress().getOwnerType() != AddressOwnerType.Supplier) {
                throw new BadRequestException("Adresse ist keinem Lieferanten zugeordnet.");
            }
            addressRequestMapper.partialUpdate(address, dto.getAddress());
            supplier.setAddress(address);
        }

        Supplier savedSupplier = supplierRepository.save(supplier);
        return ResponseEntity.ok(createSupplierResponseMapper.toDto(savedSupplier));

    }

    public boolean existsSupplierById(Integer id) {
        return supplierRepository.existsById(id);
    }

    public ResponseEntity<List<CustomerIdResponseDTO>> getCustomerIds(Integer supplierId) {
        return supplierRepository.findById(supplierId)
                .map(supplier -> {
                    if (supplier.getCustomerNumber() == null) {
                        return ResponseEntity.ok(List.<CustomerIdResponseDTO>of());
                    }
                    return ResponseEntity.ok(List.of(new CustomerIdResponseDTO(supplierId, supplier.getCustomerNumber())));
                }).orElseThrow(() -> new NotFoundException("Lieferant mit id " + supplierId + " nicht gefunden."));
    }

}
