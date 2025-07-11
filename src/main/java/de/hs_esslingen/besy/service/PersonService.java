package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.PersonRequestDTO;
import de.hs_esslingen.besy.dto.PersonResponseDTO;
import de.hs_esslingen.besy.exception.NotFoundException;
import de.hs_esslingen.besy.mapper.PersonRequestMapper;
import de.hs_esslingen.besy.mapper.PersonResponseMapper;
import de.hs_esslingen.besy.model.Address;
import de.hs_esslingen.besy.model.Faculty;
import de.hs_esslingen.besy.model.Person;
import de.hs_esslingen.besy.repository.AddressRepository;
import de.hs_esslingen.besy.repository.FacultyRepository;
import de.hs_esslingen.besy.repository.PersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final AddressRepository addressRepository;
    private final FacultyRepository facultyRepository;
    private final PersonResponseMapper personResponseMapper;
    private final PersonRequestMapper personRequestMapper;

    public ResponseEntity<List<PersonResponseDTO>> getAllPersons() {
        List<Person> persons = personRepository.findAll();
        List<PersonResponseDTO> personResponseDTOS = personResponseMapper.toDto(persons);
        return ResponseEntity.ok(personResponseDTOS);
    }

    public ResponseEntity<PersonResponseDTO> getPersonById(Long id) {
        Person person = personRepository.findById(id).orElseThrow(() -> new NotFoundException("Person with id " + id + " not found"));
        PersonResponseDTO personResponseDTO = personResponseMapper.toDto(person);
        return ResponseEntity.ok(personResponseDTO);
    }

    public ResponseEntity<PersonResponseDTO> createPerson(PersonRequestDTO personDTO) {
        Address address = addressRepository.getReferenceById(personDTO.getAddressNameId());
        Faculty faculty = facultyRepository.getReferenceById(personDTO.getFacultyAbbrId());

        Person person = personRequestMapper.toEntity(personDTO);
        person.setAddress(address);
        person.setFaculty(faculty);

        Person savedPerson = personRepository.save(person);
        return ResponseEntity.ok(personResponseMapper.toDto(savedPerson));
    }

    public ResponseEntity<PersonResponseDTO> updatePerson(Long id, PersonRequestDTO personDTO) {
        if(!personRepository.existsById(id)) return ResponseEntity.notFound().build();

        Address address = addressRepository.getReferenceById(personDTO.getAddressNameId());
        Faculty faculty = facultyRepository.getReferenceById(personDTO.getFacultyAbbrId());
        Person person = personRequestMapper.toEntity(personDTO);
        person.setPersonId(id);
        person.setAddress(address);
        person.setFaculty(faculty);

        Person savedPerson = personRepository.save(person);
        return ResponseEntity.ok(personResponseMapper.toDto(savedPerson));
    }
}
