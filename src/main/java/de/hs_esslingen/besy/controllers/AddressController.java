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

    @Deprecated
    @GetMapping
    ResponseEntity<List<AddressResponseDTO>> getAll() {
        return addressService.getAddresses();
    }

    @Deprecated
    @GetMapping("{id}")
    ResponseEntity<AddressResponseDTO> getById(@PathVariable("id") Integer id) {
        return addressService.getAddressById(id);
    }

    @Deprecated
    @PostMapping
    ResponseEntity<AddressResponseDTO> create(@RequestBody AddressRequestDTO addressRequestDTO) {
        return addressService.createAddress(addressRequestDTO);
    }
}
