package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.request.PersonRequestDTO;
import de.hs_esslingen.besy.dtos.response.PersonResponseDTO;
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

    @GetMapping
    public ResponseEntity<List<PersonResponseDTO>> getAllPersons() {
        return personService.getAllPersons();
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
            @PathVariable Long id,
            @RequestBody PersonRequestDTO personRequestDTO) {
        return personService.updatePerson(id, personRequestDTO);
    }
}
