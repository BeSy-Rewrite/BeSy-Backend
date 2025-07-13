package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.CustomerIdRequestDTO;
import de.hs_esslingen.besy.dtos.response.CustomerIdResponseDTO;
import de.hs_esslingen.besy.mappers.request.CustomerIdRequestMapper;
import de.hs_esslingen.besy.mappers.response.CustomerIdResponseMapper;
import de.hs_esslingen.besy.models.CustomerId;
import de.hs_esslingen.besy.models.CustomerIdId;
import de.hs_esslingen.besy.repositories.CustomerIdRepository;
import de.hs_esslingen.besy.repositories.SupplierRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CustomerIdService {

    private final CustomerIdRepository customerIdRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerIdResponseMapper customerIdResponseMapper;
    private final CustomerIdRequestMapper customerIdRequestMapper;

    public ResponseEntity<List<CustomerIdResponseDTO>> getAllCustomerIds() {
        List<CustomerId> customerIds = customerIdRepository.findAll();
        List<CustomerIdResponseDTO> customerIdResponseDTOS = customerIdResponseMapper.toDto(customerIds);
        return ResponseEntity.ok(customerIdResponseDTOS);
    }

    public ResponseEntity<CustomerIdResponseDTO> createCustomerId(CustomerIdRequestDTO requestDTO) {
        // if(!supplierRepository.existsById(requestDTO.getSupplierName())) throw new NotFoundException("Supplier with id " + requestDTO.getSupplierName() + " does not exist.");
        if(customerIdRepository.existsByCustomerIdAndSupplierName(requestDTO.getCustomerId(), requestDTO.getSupplierName()))
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        CustomerIdId customerIdId = new CustomerIdId();
        customerIdId.setCustomerId(requestDTO.getCustomerId());
        customerIdId.setSupplierName(requestDTO.getSupplierName());

        CustomerId customerId = customerIdRequestMapper.toEntity(requestDTO);
        customerId.setId(customerIdId);
        customerId.setSupplier(supplierRepository.getReferenceById(requestDTO.getSupplierName()));

        customerIdRepository.save(customerId);
        return ResponseEntity.ok(customerIdResponseMapper.toDto(customerId));
    }
}
