package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.response.AddressResponseDTO;
import de.hs_esslingen.besy.services.AddressService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/addresses")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    ResponseEntity<List<AddressResponseDTO>> getAll() {
        return addressService.getAddresses();
    }

    @GetMapping("{id}")
    ResponseEntity<AddressResponseDTO> getById(@PathVariable("id") Integer id) {
        return addressService.getAddressById(id);
    }
}
