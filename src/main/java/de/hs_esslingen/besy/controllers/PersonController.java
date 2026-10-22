package de.hs_esslingen.besy.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hs_esslingen.besy.dtos.request.AddressRequestDTO;
import de.hs_esslingen.besy.dtos.request.PersonRequestDTO;
import de.hs_esslingen.besy.dtos.response.AddressResponseDTO;
import de.hs_esslingen.besy.dtos.response.PersonResponseDTO;
import de.hs_esslingen.besy.enums.AddressOwnerType;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.services.AddressService;
import de.hs_esslingen.besy.services.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(value = "${api.prefix}/persons", produces = "application/json")
public class PersonController {

    private final PersonService personService;
    private final AddressService addressService;

    @GetMapping
    @Operation(description = "Gibt alle Personen zurück.")
    @ApiResponse(responseCode = "200", description = "Liste aller Personen.")
    public ResponseEntity<List<PersonResponseDTO>> getAllPersons() {
        return personService.getAllPersons();
    }

    @GetMapping("/{id}")
    @Operation(description = "Gibt eine Person anhand ihrer ID zurück.")
    @ApiResponse(responseCode = "200", description = "Person erfolgreich abgerufen.")
    @ApiResponse(responseCode = "404", description = "Person nicht gefunden.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<PersonResponseDTO> getPersonById(
            @Parameter(description = "Die ID der abzurufenden Person.") @PathVariable("id") long id) {
        return personService.getPersonById(id);
    }

    @PostMapping
    @Operation(description = "Erstellt eine neue Person.")
    @ApiResponse(responseCode = "200", description = "Person erfolgreich erstellt.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<PersonResponseDTO> createPerson(@RequestBody PersonRequestDTO personRequestDTO) {
        return personService.createPerson(personRequestDTO);
    }

    @PutMapping("/{id}")
    @Operation(description = "Aktualisiert eine bestehende Person anhand ihrer ID.")
    @ApiResponse(responseCode = "200", description = "Person erfolgreich aktualisiert.")
    @ApiResponse(responseCode = "409", description = "Verweis auf eine nicht existierende Entität.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<PersonResponseDTO> updatePerson(
            @Parameter(description = "Die ID der zu aktualisierenden Person.") @PathVariable("id") Long id,
            @RequestBody PersonRequestDTO personRequestDTO) {
        if (!personService.existsById(id))
            throw new NotFoundException("Person nicht gefunden.");
        return personService.updatePerson(id, personRequestDTO);
    }

    /**
     * @deprecated Remove this endpoint in a future release.
     */
    @Deprecated(since = "api-1.10.x")
    @GetMapping("/addresses")
    @Operation(description = "Gibt eine Liste aller Adressen zurück, die Personen zugeordnet sind.")
    @ApiResponse(responseCode = "200", description = "Erfolgreich - Liste der Personenadressen")
    public ResponseEntity<List<AddressResponseDTO>> getAddresses() {
        return addressService.getPersonAddresses();
    }

    @PostMapping("/addresses")
    @Operation(description = "Erstellt eine Adresse für eine Person.")
    @ApiResponse(responseCode = "201", description = "Adresse erfolgreich erstellt.")
    @ApiResponse(responseCode = "400", description = "Ungültige Eingabe(n).")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<AddressResponseDTO> createAddress(@RequestBody AddressRequestDTO addressRequestDTO) {
        return addressService.createAddress(addressRequestDTO, AddressOwnerType.Person);
    }

    @GetMapping("/{person-id}/address")
    @Operation(description = "Gibt die Adresse der angegebenen Person zurück.")
    @ApiResponse(responseCode = "200", description = "Erfolgreich - Adresse der Person")
    @ApiResponse(responseCode = "404", description = "Person nicht gefunden. Diese Person besitzt keine Adresse.")
    public ResponseEntity<AddressResponseDTO> getAddressOfPerson(
            @Parameter(description = "Die ID der Person, deren Adresse abgerufen werden soll") @PathVariable("person-id") Long id) {
        if (!personService.existsById(id))
            throw new NotFoundException("Person nicht gefunden.");
        return addressService.getAddressOfPerson(id);
    }

}
