package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.SupplierPOSTRequestDTO;
import de.hs_esslingen.besy.dtos.request.SupplierPUTRequestDTO;
import de.hs_esslingen.besy.dtos.response.SupplierResponseDTO;
import de.hs_esslingen.besy.mappers.request.SupplierPUTRequestMapper;
import de.hs_esslingen.besy.mappers.request.SupplierRequestMapper;
import de.hs_esslingen.besy.mappers.response.SupplierResponseMapper;
import de.hs_esslingen.besy.models.AddressType;
import de.hs_esslingen.besy.models.Country;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.repositories.AddressTypeRepository;
import de.hs_esslingen.besy.repositories.CountryRepository;
import de.hs_esslingen.besy.repositories.SupplierRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SupplierService {

    private SupplierRepository supplierRepository;
    private final AddressTypeRepository addressTypeRepository;
    private final CountryRepository countryRepository;
    private final SupplierResponseMapper supplierResponseMapper;
    private final SupplierRequestMapper supplierRequestMapper;
    private final SupplierPUTRequestMapper supplierPUTRequestMapper;

    public ResponseEntity<List<SupplierResponseDTO>> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAll();
        List<SupplierResponseDTO> supplierResponseDTOS = supplierResponseMapper.toDto(suppliers);
        return ResponseEntity.ok(supplierResponseDTOS);
    }

    public ResponseEntity<SupplierResponseDTO> createSupplier(SupplierPOSTRequestDTO supplierDTO) {
        if(supplierRepository.existsById(supplierDTO.getSupplierName())) return ResponseEntity.status(HttpStatus.CONFLICT).build();

        Country countryRef = countryRepository.getReferenceById(supplierDTO.getCountryName());
        AddressType addressTypeRef = addressTypeRepository.getReferenceById(supplierDTO.getAddressType());

        Supplier supplier = supplierRequestMapper.toEntity(supplierDTO);
        supplier.setCountry(countryRef);
        supplier.setAddress(addressTypeRef);

        Supplier savedSupplier = supplierRepository.save(supplier);
        return ResponseEntity.ok(supplierResponseMapper.toDto(savedSupplier));
    }

    public ResponseEntity<SupplierResponseDTO> updateSupplier(String supplierName, SupplierPUTRequestDTO supplierDTO) {
        if(!supplierRepository.existsById(supplierName)) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Country countryRef = countryRepository.getReferenceById(supplierDTO.getCountryName());
        AddressType addressTypeRef = addressTypeRepository.getReferenceById(supplierDTO.getAddressType());

        Supplier supplier = supplierPUTRequestMapper.toEntity(supplierDTO);
        supplier.setSupplierName(supplierName);
        supplier.setCountry(countryRef);
        supplier.setAddress(addressTypeRef);

        Supplier savedSupplier = supplierRepository.save(supplier);
        return ResponseEntity.ok(supplierResponseMapper.toDto(savedSupplier));
    }
}
