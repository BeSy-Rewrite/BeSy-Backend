package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.dto.PersonRequestDTO;
import de.hs_esslingen.besy.dto.PersonResponseDTO;
import de.hs_esslingen.besy.service.PersonService;
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
