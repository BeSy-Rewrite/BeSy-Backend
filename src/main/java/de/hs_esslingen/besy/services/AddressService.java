package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.AddressRequestDTO;
import de.hs_esslingen.besy.dtos.response.AddressResponseDTO;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.request.AddressRequestMapper;
import de.hs_esslingen.besy.mappers.response.AddressResponseMapper;
import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.repositories.AddressRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressRequestMapper addressRequestMapper;
    private final AddressResponseMapper addressResponseMapper;

    public ResponseEntity<List<AddressResponseDTO>> getAddresses() {
        List<Address> addresses = addressRepository.findAll();
        List<AddressResponseDTO> responseDTOS = addressResponseMapper.toDto(addresses);
        return ResponseEntity.ok(responseDTOS);
    }

    public ResponseEntity<AddressResponseDTO> getAddressById(Integer id) {
        return addressRepository.findById(id)
                .map(address -> ResponseEntity.ok(addressResponseMapper.toDto(address)))
                .orElseThrow(() -> new NotFoundException("Adresse mit id " + id + " existiert nicht."));
    }

}
