package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.request.AddressRequestDTO;
import de.hs_esslingen.besy.dtos.request.PersonRequestDTO;
import de.hs_esslingen.besy.dtos.request.PersonUpdateRequestDTO;
import de.hs_esslingen.besy.dtos.response.AddressResponseDTO;
import de.hs_esslingen.besy.dtos.response.PersonResponseDTO;
import de.hs_esslingen.besy.enums.AddressOwnerType;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.services.AddressService;
import de.hs_esslingen.besy.services.PersonService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/persons")
public class PersonController {

    private final PersonService personService;
    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<PersonResponseDTO>> getAllPersons(
            @RequestParam(required = false, defaultValue = "true") Boolean active) {
        return personService.getAllPersons(active);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponseDTO> getPersonById(@PathVariable("id") long id) {
        return personService.getPersonById(id);
    }

    @PostMapping
    public ResponseEntity<PersonResponseDTO> createPerson(@RequestBody PersonRequestDTO personRequestDTO) {
        return personService.createPerson(personRequestDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonResponseDTO> updatePerson(
            @PathVariable("id") Long id,
            @RequestBody PersonUpdateRequestDTO personUpdateRequestDTO) {
        if(!personService.existsById(id)) throw new NotFoundException("Person nicht gefunden.");
        return personService.updatePerson(id, personUpdateRequestDTO);
    }

    @Deprecated(since = "api-1.8.x")
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressResponseDTO>> getAddresses() {
        return addressService.getPersonAddresses();
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressResponseDTO> createAddress(@RequestBody AddressRequestDTO addressRequestDTO) {
        return addressService.createAddress(addressRequestDTO, AddressOwnerType.Person);
    }

    @GetMapping("/{person-id}/address")
    public ResponseEntity<AddressResponseDTO> getAddressOfPerson(@PathVariable("person-id") Long id) {
        if(!personService.existsById(id)) throw new NotFoundException("Person nicht gefunden.");
        return addressService.getAddressOfPerson(id);
    }

}
