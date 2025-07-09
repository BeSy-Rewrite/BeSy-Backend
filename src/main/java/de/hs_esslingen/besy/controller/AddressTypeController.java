package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.model.AddressType;
import de.hs_esslingen.besy.repository.AddressTypeRepository;
import de.hs_esslingen.besy.service.AddressTypeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/address-types")
public class AddressTypeController {

    private final AddressTypeService addressTypeService;

    @GetMapping
    public ResponseEntity<List<AddressType>> getAddressTypes() {
        return ResponseEntity.ok(addressTypeService.getAllAddressTypes());
    }
}
