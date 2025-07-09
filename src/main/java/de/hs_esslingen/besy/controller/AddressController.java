package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.dto.AddressDto;
import de.hs_esslingen.besy.dto.AddressDto1;
import de.hs_esslingen.besy.model.Address;
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
    public ResponseEntity<List<Address>> getAddressTypes() {
        return addressService.getAllAddresses();
    }

    @PostMapping
    public ResponseEntity<AddressDto> createAddress(@RequestBody AddressDto1 addressDTO) {
        return addressService.addAddress(addressDTO);
    }
}
