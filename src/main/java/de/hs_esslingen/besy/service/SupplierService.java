package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.request.SupplierRequestDTO;
import de.hs_esslingen.besy.dto.response.SupplierResponseDTO;
import de.hs_esslingen.besy.mapper.request.SupplierRequestMapper;
import de.hs_esslingen.besy.mapper.response.SupplierResponseMapper;
import de.hs_esslingen.besy.model.AddressType;
import de.hs_esslingen.besy.model.Country;
import de.hs_esslingen.besy.model.Supplier;
import de.hs_esslingen.besy.repository.AddressTypeRepository;
import de.hs_esslingen.besy.repository.CountryRepository;
import de.hs_esslingen.besy.repository.SupplierRepository;
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

    public ResponseEntity<List<SupplierResponseDTO>> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAll();
        List<SupplierResponseDTO> supplierResponseDTOS = supplierResponseMapper.toDto(suppliers);
        return ResponseEntity.ok(supplierResponseDTOS);
    }

    public ResponseEntity<SupplierResponseDTO> createSupplier(SupplierRequestDTO supplierDTO) {
        if(supplierRepository.existsById(supplierDTO.getSupplierName())) return ResponseEntity.status(HttpStatus.CONFLICT).build();

        Country countryRef = countryRepository.getReferenceById(supplierDTO.getCountryName());
        AddressType addressTypeRef = addressTypeRepository.getReferenceById(supplierDTO.getAddressType());

        Supplier supplier = supplierRequestMapper.toEntity(supplierDTO);
        supplier.setCountry(countryRef);
        supplier.setAddress(addressTypeRef);

        Supplier savedSupplier = supplierRepository.save(supplier);
        return ResponseEntity.ok(supplierResponseMapper.toDto(savedSupplier));
    }

    public ResponseEntity<SupplierResponseDTO> updateSupplier(String supplierName, SupplierRequestDTO supplierDTO) {
        if(!supplierRepository.existsById(supplierName)) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Country countryRef = countryRepository.getReferenceById(supplierDTO.getCountryName());
        AddressType addressTypeRef = addressTypeRepository.getReferenceById(supplierDTO.getAddressType());

        Supplier supplier = supplierRequestMapper.toEntity(supplierDTO);
        supplier.setSupplierName(supplierName);
        supplier.setCountry(countryRef);
        supplier.setAddress(addressTypeRef);

        Supplier savedSupplier = supplierRepository.save(supplier);
        return ResponseEntity.ok(supplierResponseMapper.toDto(savedSupplier));
    }
}
