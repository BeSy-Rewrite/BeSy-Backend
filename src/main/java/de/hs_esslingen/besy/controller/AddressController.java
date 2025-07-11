package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.dto.AddressRequestDTO;
import de.hs_esslingen.besy.dto.AddressResponseDTO;
import de.hs_esslingen.besy.service.AddressService;
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
