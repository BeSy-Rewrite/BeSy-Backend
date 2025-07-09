package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.AddressDto;
import de.hs_esslingen.besy.dto.AddressDto1;
import de.hs_esslingen.besy.mapper.AddressMapper;
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
    private final AddressMapper addressMapper;


    public ResponseEntity<List<Address>> getAllAddresses() {
        return new ResponseEntity<>(addressRepository.findAll(), HttpStatus.OK);
    }

    public ResponseEntity<AddressDto> addAddress(AddressDto1 addressDTO) {
        if(addressRepository.findById(addressDTO.getAddressName()).isPresent()) return ResponseEntity.status(HttpStatus.CONFLICT).body(null);

        Country countryRef = countryRepository.getReferenceById(addressDTO.getCountryName());
        AddressType addressTypeRef = addressTypeRepository.getReferenceById(addressDTO.getAddressType());

        System.out.println("Country: " + countryRef.getCountryName() + " AddressType: " + addressTypeRef.getAddressType());

        Address address = addressMapper.toEntity(addressDTO);
        System.out.println("Address: " + address);
        address.setCountryName(countryRef);
        address.setAddressType(addressTypeRef);

        Address savedAddress = addressRepository.save(address);
        return ResponseEntity.ok().body(addressMapper.toDto1(savedAddress));
    }
}
