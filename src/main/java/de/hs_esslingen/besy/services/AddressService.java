package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.AddressRequestDTO;
import de.hs_esslingen.besy.dtos.response.AddressResponseDTO;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.request.AddressRequestMapper;
import de.hs_esslingen.besy.mappers.response.AddressResponseMapper;
import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.repositories.AddressRepository;
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

    @Deprecated
    public ResponseEntity<AddressResponseDTO> createAddress(
            AddressRequestDTO dto
    ) {
        Address address = addressRequestMapper.toEntity(dto);
        Address addressPersisted = addressRepository.save(address);
        addressRepository.flush();
        return ResponseEntity.ok(addressResponseMapper.toDto(addressPersisted));
    }


    public ResponseEntity<List<AddressResponseDTO>> getSupplierAddresses() {
        List<Integer> addressIds = supplierRepository.findAllAddressId();
        List<Address> addresses = addressRepository.findAllById(addressIds);
        return ResponseEntity.ok(addressResponseMapper.toDto(addresses));
    }

    public ResponseEntity<AddressResponseDTO> getAddressOfSupplier(Integer supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId).get();
        if(!existsById(supplier.getAddressId())) throw new NotFoundException("Dieser Lieferant besitzt keine Adresse.");
        Address address = supplier.getAddress();
        return ResponseEntity.ok(addressResponseMapper.toDto(address));
    }

    public boolean existsById(Integer id) {
        return addressRepository.existsById(id);
    }

}
