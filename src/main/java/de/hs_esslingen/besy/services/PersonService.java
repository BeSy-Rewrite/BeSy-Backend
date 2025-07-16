package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.PersonRequestDTO;
import de.hs_esslingen.besy.dtos.response.PersonResponseDTO;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.request.PersonRequestMapper;
import de.hs_esslingen.besy.mappers.response.PersonResponseMapper;
import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.models.Faculty;
import de.hs_esslingen.besy.models.Person;
import de.hs_esslingen.besy.repositories.AddressRepository;
import de.hs_esslingen.besy.repositories.FacultyRepository;
import de.hs_esslingen.besy.repositories.PersonRepository;
import lombok.AllArgsConstructor;
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
        Address address = addressRepository.getReferenceById(personDTO.getAddressName());
        Faculty faculty = facultyRepository.getReferenceById(personDTO.getFacultyAbbr());

        Person person = personRequestMapper.toEntity(personDTO);
        person.setAddress(address);
        person.setFaculty(faculty);

        Person savedPerson = personRepository.save(person);
        return ResponseEntity.ok(personResponseMapper.toDto(savedPerson));
    }

    public ResponseEntity<PersonResponseDTO> updatePerson(Long id, PersonRequestDTO personDTO) {
        if(!personRepository.existsById(id)) return ResponseEntity.notFound().build();

        Address address = addressRepository.getReferenceById(personDTO.getAddressName());
        Faculty faculty = facultyRepository.getReferenceById(personDTO.getFacultyAbbr());
        Person person = personRequestMapper.toEntity(personDTO);
        person.setPersonId(id);
        person.setAddress(address);
        person.setFaculty(faculty);

        Person savedPerson = personRepository.save(person);
        return ResponseEntity.ok(personResponseMapper.toDto(savedPerson));
    }
}
