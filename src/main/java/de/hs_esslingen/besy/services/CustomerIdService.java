package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.CustomerIdRequestDTO;
import de.hs_esslingen.besy.dtos.response.CustomerIdResponseDTO;
import de.hs_esslingen.besy.mappers.request.CustomerIdRequestMapper;
import de.hs_esslingen.besy.mappers.response.CustomerIdResponseMapper;
import de.hs_esslingen.besy.models.CustomerId;
import de.hs_esslingen.besy.models.CustomerIdId;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.repositories.CustomerIdRepository;
import de.hs_esslingen.besy.repositories.SupplierRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CustomerIdService {

    private final CustomerIdRepository customerIdRepository;
    private final CustomerIdResponseMapper customerIdResponseMapper;
    private final CustomerIdRequestMapper customerIdRequestMapper;
    private final SupplierRepository supplierRepository;

    public ResponseEntity<List<CustomerIdResponseDTO>> getAllCustomerIds(Integer supplierId) {
        List<CustomerId> customerIds = customerIdRepository.findAllBySupplierId(supplierId);
        List<CustomerIdResponseDTO> customerIdResponseDTOS = customerIdResponseMapper.toDto(customerIds);
        return ResponseEntity.ok(customerIdResponseDTOS);
    }

    public ResponseEntity<CustomerIdResponseDTO> createCustomerId(Integer supplierId, CustomerIdRequestDTO dto) {
        CustomerIdId customerIdId = new CustomerIdId(dto.getCustomerId(), supplierId);
        if(customerIdRepository.existsById(customerIdId)) throw new RuntimeException("Lieferanten-ID existiert bereits.");

        Supplier supplier = supplierRepository.getReferenceById(supplierId);
        CustomerId customerId = customerIdRequestMapper.toEntity(dto);

        customerId.setSupplier(supplier);
        customerId.setId(customerIdId);
        CustomerId customerIdPersisted = customerIdRepository.save(customerId);
        return ResponseEntity.ok(customerIdResponseMapper.toDto(customerIdPersisted));
    }
}
