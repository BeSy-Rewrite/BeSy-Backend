package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.response.CustomerIdRequestDTO;
import de.hs_esslingen.besy.dto.response.CustomerIdResponseDTO;
import de.hs_esslingen.besy.exception.NotFoundException;
import de.hs_esslingen.besy.mapper.request.CustomerIdRequestMapper;
import de.hs_esslingen.besy.mapper.response.CustomerIdResponseMapper;
import de.hs_esslingen.besy.model.CustomerId;
import de.hs_esslingen.besy.model.CustomerIdId;
import de.hs_esslingen.besy.model.Supplier;
import de.hs_esslingen.besy.repository.CustomerIdRepository;
import de.hs_esslingen.besy.repository.SupplierRepository;
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
