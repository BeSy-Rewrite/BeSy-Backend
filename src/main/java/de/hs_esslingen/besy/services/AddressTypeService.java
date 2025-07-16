package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.AddressTypeResponseDTO;
import de.hs_esslingen.besy.mappers.AddressTypeMapper;
import de.hs_esslingen.besy.repositories.AddressTypeRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AddressTypeService {

    private final AddressTypeRepository addressTypeRepository;
    private final AddressTypeMapper addressTypeMapper;

    public ResponseEntity<List<AddressTypeResponseDTO>> getAllAddressTypes() {
        List<AddressType> addressTypes = addressTypeRepository.findAll();
        List<AddressTypeResponseDTO> addressTypeResponseDTOS = addressTypeMapper.toDto(addressTypes);
        return ResponseEntity.ok(addressTypeResponseDTOS);
    }
}
