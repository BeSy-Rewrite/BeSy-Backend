package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.PersonRequestDTO;
import de.hs_esslingen.besy.dtos.response.PersonResponseDTO;
import de.hs_esslingen.besy.enums.AddressOwnerType;
import de.hs_esslingen.besy.exceptions.BadRequestException;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.request.PersonRequestMapper;
import de.hs_esslingen.besy.mappers.response.PersonResponseMapper;
import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.models.Person;
import de.hs_esslingen.besy.repositories.AddressRepository;
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
    private final PersonResponseMapper personResponseMapper;
    private final PersonRequestMapper personRequestMapper;

    public ResponseEntity<List<PersonResponseDTO>> getAllPersons() {
        List<Person> persons = personRepository.findAll();
        List<PersonResponseDTO> personResponseDTOS = personResponseMapper.toDto(persons);
        return ResponseEntity.ok(personResponseDTOS);
    }

    public ResponseEntity<PersonResponseDTO> getPersonById(Long id) {
        return personRepository.findById(id)
                .map(person -> ResponseEntity.ok(personResponseMapper.toDto(person)))
                .orElseThrow(() -> new NotFoundException("Person mit id " + id + " nicht gefunden."));
    }

    public ResponseEntity<PersonResponseDTO> createPerson(PersonRequestDTO personDTO) {
        Person person = personRequestMapper.toEntity(personDTO);

        if(personDTO.getAddressId() != null){
            Address address = addressRepository.getReferenceById(personDTO.getAddressId());
            if(address.getOwnerType() != AddressOwnerType.Person) {
                throw new BadRequestException("Adresse ist keiner Person zugeordnet.");
            }
            person.setAddress(address);
        }

        Person savedPerson = personRepository.save(person);
        return ResponseEntity.ok(personResponseMapper.toDto(savedPerson));
    }


    public ResponseEntity<PersonResponseDTO> updatePerson(Long id, PersonRequestDTO personDTO) {
        Person person = personRepository.findById(id).get();
        personRequestMapper.partialUpdate(person, personDTO);

        if(personDTO.getAddressId() != null){
            Address address = addressRepository.getReferenceById(personDTO.getAddressId());
            if(address.getOwnerType() != AddressOwnerType.Person) {
                throw new BadRequestException("Adresse ist keiner Person zugeordnet.");
            }
            person.setAddress(address);
        }

        Person savedPerson = personRepository.save(person);
        return ResponseEntity.ok(personResponseMapper.toDto(savedPerson));
    }

    public boolean existsById(Long id) {
        return personRepository.existsById(id);
    }


}
