package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.model.AddressType;
import de.hs_esslingen.besy.repository.AddressTypeRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AddressTypeService {

    private final AddressTypeRepository addressTypeRepository;

    public ResponseEntity<List<AddressType>> getAllAddressTypes() {
        List<AddressType> addressTypes = addressTypeRepository.findAll();
    }
}
