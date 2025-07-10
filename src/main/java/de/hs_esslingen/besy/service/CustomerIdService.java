package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.CustomerIdResponseDTO;
import de.hs_esslingen.besy.mapper.CustomerIdResponseMapper;
import de.hs_esslingen.besy.model.CustomerId;
import de.hs_esslingen.besy.repository.CustomerIdRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CustomerIdService {

    private final CustomerIdRepository customerIdRepository;
    private final CustomerIdResponseMapper customerIdResponseMapper;

    public ResponseEntity<List<CustomerIdResponseDTO>> getAllCustomerIds() {
        List<CustomerId> customerIds = customerIdRepository.findAll();
        List<CustomerIdResponseDTO> customerIdResponseDTOS = customerIdResponseMapper.toDto(customerIds);
        return ResponseEntity.ok(customerIdResponseDTOS);

    }
}
