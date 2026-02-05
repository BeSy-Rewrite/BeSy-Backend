package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.AddressRequestDTO;
import de.hs_esslingen.besy.dtos.response.AddressResponseDTO;
import de.hs_esslingen.besy.enums.AddressOwnerType;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.request.AddressRequestMapper;
import de.hs_esslingen.besy.mappers.response.AddressResponseMapper;
import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.models.Person;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.repositories.AddressRepository;
import de.hs_esslingen.besy.repositories.PersonRepository;
import de.hs_esslingen.besy.repositories.SupplierRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final SupplierRepository supplierRepository;
    private final PersonRepository personRepository;

    private final AddressRequestMapper addressRequestMapper;
    private final AddressResponseMapper addressResponseMapper;

    private final SupplierService supplierService;

    @Deprecated
    public ResponseEntity<List<AddressResponseDTO>> getAddresses() {
        List<Address> addresses = addressRepository.findAll();
        List<AddressResponseDTO> responseDTOS = addressResponseMapper.toDto(addresses);
        return ResponseEntity.ok(responseDTOS);
    }

    @Deprecated
    public ResponseEntity<AddressResponseDTO> getAddressById(Integer id) {
        return addressRepository.findById(id)
                .map(address -> ResponseEntity.ok(addressResponseMapper.toDto(address)))
                .orElseThrow(() -> new NotFoundException("Adresse mit id " + id + " existiert nicht."));
    }

    public ResponseEntity<AddressResponseDTO> createAddress(
            AddressRequestDTO dto, AddressOwnerType addressOwnerType
    ) {
        Address address = addressRequestMapper.toEntity(dto);
        address.setOwnerType(addressOwnerType);
        Address addressPersisted = addressRepository.save(address);
        return ResponseEntity.ok(addressResponseMapper.toDto(addressPersisted));
    }


    public ResponseEntity<List<AddressResponseDTO>> getSupplierAddresses() {
        List<Address> addresses = addressRepository.getAddressByOwnerType(AddressOwnerType.Supplier);
        return ResponseEntity.ok(addressResponseMapper.toDto(addresses));
    }

    public ResponseEntity<AddressResponseDTO> getAddressOfSupplier(Integer supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException("Lieferant nicht gefunden."));

        if(supplier.getAddressId() == null || !existsById(supplier.getAddressId())) throw new NotFoundException("Dieser Lieferant besitzt keine Adresse.");
        Address address = supplier.getAddress();

        if (address.getOwnerType() != AddressOwnerType.Supplier) {
            throw new NotFoundException("Dieser Lieferant besitzt keine gültige Adresse.");
        }

        return ResponseEntity.ok(addressResponseMapper.toDto(address));
    }

    public ResponseEntity<List<AddressResponseDTO>> getPersonAddresses() {
        List<Address> addresses = addressRepository.getAddressByOwnerType(AddressOwnerType.Person);
        return ResponseEntity.ok(addressResponseMapper.toDto(addresses));
    }

    public ResponseEntity<AddressResponseDTO> getAddressOfPerson(Long personId) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new NotFoundException("Person nicht gefunden."));

        if(person.getAddressId() == null || !existsById(person.getAddressId())) throw new NotFoundException("Diese Person besitzt keine Adresse.");
        Address address = person.getAddress();

        if (address.getOwnerType() != AddressOwnerType.Person) {
            throw new NotFoundException("Diese Person besitzt keine gültige Adresse.");
        }

        return ResponseEntity.ok(addressResponseMapper.toDto(address));
    }

    public boolean existsById(Integer id) {
        return addressRepository.existsById(id);
    }

}
