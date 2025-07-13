package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.request.AddressRequestDTO;
import de.hs_esslingen.besy.dtos.response.AddressResponseDTO;
import de.hs_esslingen.besy.services.AddressService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/addresses")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getAddressTypes() {
        return addressService.getAllAddresses();
    }

    @PostMapping
    public ResponseEntity<AddressResponseDTO> createAddress(@RequestBody AddressRequestDTO addressDTO) {
        return addressService.addAddress(addressDTO);
    }

    @PutMapping
    @RequestMapping("/{address-name}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable("address-name") String addressName,
            @RequestBody AddressRequestDTO addressDTO) {
        return addressService.updateAddress(addressName, addressDTO);
    }
}
