package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.AddressRequestDTO;
import de.hs_esslingen.besy.dtos.response.AddressResponseDTO;
import de.hs_esslingen.besy.mappers.request.AddressRequestMapper;
import de.hs_esslingen.besy.mappers.response.AddressResponseMapper;
import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.repositories.AddressRepository;
import de.hs_esslingen.besy.repositories.AddressTypeRepository;
import de.hs_esslingen.besy.repositories.CountryRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final CountryRepository countryRepository;
    private final AddressTypeRepository addressTypeRepository;
    private final AddressResponseMapper addressResponseMapper;
    private final AddressRequestMapper addressRequestMapper;


    public ResponseEntity<List<AddressResponseDTO>> getAllAddresses() {
        List<Address> addresses = addressRepository.findAll();
        List<AddressResponseDTO> addressResponseDTOS = addressResponseMapper.toDto(addresses);
        return ResponseEntity.ok(addressResponseDTOS);
    }

    public ResponseEntity<AddressResponseDTO> addAddress(AddressRequestDTO addressDTO) {
        if(addressRepository.existsById(addressDTO.getAddressName())) return ResponseEntity.status(HttpStatus.CONFLICT).build();

        Country countryRef = countryRepository.getReferenceById(addressDTO.getCountryName());
        AddressType addressTypeRef = addressTypeRepository.getReferenceById(addressDTO.getAddressType());

        Address address = addressRequestMapper.toEntity(addressDTO);
        address.setCountry(countryRef);
        address.setAddressTypeRef(addressTypeRef);

        Address savedAddress = addressRepository.save(address);
        return ResponseEntity.ok().body(addressResponseMapper.toDto(savedAddress));
    }

    public ResponseEntity<AddressResponseDTO> updateAddress(String addressName, AddressRequestDTO addressDTO) {
        if(!addressRepository.existsById(addressName)) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Country countryRef = countryRepository.getReferenceById(addressDTO.getCountryName());
        AddressType addressTypeRef = addressTypeRepository.getReferenceById(addressDTO.getAddressType());

        Address updatedAddress = addressRequestMapper.toEntity(addressDTO);
        updatedAddress.setAddressName(addressName);
        updatedAddress.setCountry(countryRef);
        updatedAddress.setAddressTypeRef(addressTypeRef);
        Address savedAddress = addressRepository.save(updatedAddress);
        return ResponseEntity.ok(addressResponseMapper.toDto(savedAddress));
    }
}
