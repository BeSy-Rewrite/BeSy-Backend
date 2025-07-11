package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.AddressRequestDTO;
import de.hs_esslingen.besy.dto.AddressResponseDTO;
import de.hs_esslingen.besy.mapper.AddressRequestMapper;
import de.hs_esslingen.besy.mapper.AddressResponseMapper;
import de.hs_esslingen.besy.model.Address;
import de.hs_esslingen.besy.model.AddressType;
import de.hs_esslingen.besy.model.Country;
import de.hs_esslingen.besy.repository.AddressRepository;
import de.hs_esslingen.besy.repository.AddressTypeRepository;
import de.hs_esslingen.besy.repository.CountryRepository;
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

        Country countryRef = countryRepository.getReferenceById(addressDTO.getCountryNameId());
        AddressType addressTypeRef = addressTypeRepository.getReferenceById(addressDTO.getAddressTypeId());

        Address address = addressRequestMapper.toEntity(addressDTO);
        address.setCountry(countryRef);
        address.setAddressTypeRef(addressTypeRef);

        Address savedAddress = addressRepository.save(address);
        return ResponseEntity.ok().body(addressResponseMapper.toDto(savedAddress));
    }

    public ResponseEntity<AddressResponseDTO> updateAddress(String addressName, AddressRequestDTO addressDTO) {
        if(!addressRepository.existsById(addressName)) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Country countryRef = countryRepository.getReferenceById(addressDTO.getCountryNameId());
        AddressType addressTypeRef = addressTypeRepository.getReferenceById(addressDTO.getAddressTypeId());

        Address updatedAddress = addressRequestMapper.toEntity(addressDTO);
        updatedAddress.setAddressName(addressName);
        updatedAddress.setCountry(countryRef);
        updatedAddress.setAddressTypeRef(addressTypeRef);
        Address savedAddress = addressRepository.save(updatedAddress);
        return ResponseEntity.ok(addressResponseMapper.toDto(savedAddress));
    }
}
