package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.PersonRequestDTO;
import de.hs_esslingen.besy.dtos.response.PersonResponseDTO;
import de.hs_esslingen.besy.enums.AddressOwnerType;
import de.hs_esslingen.besy.enums.Gender;
import de.hs_esslingen.besy.exceptions.BadRequestException;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.request.PersonRequestMapper;
import de.hs_esslingen.besy.mappers.response.PersonResponseMapper;
import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.models.Person;
import de.hs_esslingen.besy.repositories.AddressRepository;
import de.hs_esslingen.besy.repositories.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PersonResponseMapper personResponseMapper;

    @Mock
    private PersonRequestMapper personRequestMapper;

    @InjectMocks
    private PersonService personService;

    private Person person;
    private Address address;
    private PersonRequestDTO requestDtoNoAddress;
    private PersonRequestDTO requestDtoWithAddress;
    private PersonResponseDTO responseDto;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(1L);
        person.setName("Jane");
        person.setSurname("Doe");
        person.setEmail("jane.doe@example.com");
        person.setFax("123");
        person.setPhone("456");
        person.setTitle("Dr.");
        person.setComment("Comment");
        person.setGender(Gender.f);

        address = new Address();
        address.setId(10);
        address.setOwnerType(AddressOwnerType.Person);

        requestDtoNoAddress = new PersonRequestDTO(
                "Jane",
                "Doe",
                "jane.doe@example.com",
                "123",
                "456",
                "Dr.",
                "Comment",
                null,
                Gender.f
        );

        requestDtoWithAddress = new PersonRequestDTO(
                "Jane",
                "Doe",
                "jane.doe@example.com",
                "123",
                "456",
                "Dr.",
                "Comment",
                10,
                Gender.f
        );

        responseDto = new PersonResponseDTO(
                1L,
                "Jane",
                "Doe",
                "jane.doe@example.com",
                "123",
                "456",
                "Dr.",
                "Comment",
                10,
                Gender.f
        );
    }

    @Test
    void should_get_all_persons() {
        List<Person> persons = List.of(person);
        List<PersonResponseDTO> dtos = List.of(responseDto);

        when(personRepository.findAll()).thenReturn(persons);
        when(personResponseMapper.toDto(persons)).thenReturn(dtos);

        ResponseEntity<List<PersonResponseDTO>> response = personService.getAllPersons();

        assertEquals(200, response.getStatusCode().value());
        assertSame(dtos, response.getBody());
        verify(personRepository).findAll();
        verify(personResponseMapper).toDto(persons);
    }

    @Test
    void should_get_person_by_id_when_exists() {
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(personResponseMapper.toDto(person)).thenReturn(responseDto);

        ResponseEntity<PersonResponseDTO> response = personService.getPersonById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertSame(responseDto, response.getBody());
        verify(personRepository).findById(1L);
        verify(personResponseMapper).toDto(person);
    }

    @Test
    void should_throw_not_found_when_person_missing() {
        when(personRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> personService.getPersonById(1L));

        assertTrue(ex.getMessage().contains("1"));
        verify(personRepository).findById(1L);
    }

    @Test
    void should_create_person_without_address() {
        when(personRequestMapper.toEntity(requestDtoNoAddress)).thenReturn(person);
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(personResponseMapper.toDto(any(Person.class))).thenReturn(responseDto);

        ResponseEntity<PersonResponseDTO> response = personService.createPerson(requestDtoNoAddress);

        assertEquals(200, response.getStatusCode().value());
        assertSame(responseDto, response.getBody());
        verify(personRequestMapper).toEntity(requestDtoNoAddress);
        verify(personRepository).save(person);
        verify(personResponseMapper).toDto(person);
        verify(addressRepository, never()).getReferenceById(anyInt());
    }

    @Test
    void should_create_person_with_address() {
        when(personRequestMapper.toEntity(requestDtoWithAddress)).thenReturn(person);
        when(addressRepository.getReferenceById(10)).thenReturn(address);
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(personResponseMapper.toDto(any(Person.class))).thenReturn(responseDto);

        ResponseEntity<PersonResponseDTO> response = personService.createPerson(requestDtoWithAddress);

        assertEquals(200, response.getStatusCode().value());
        assertSame(responseDto, response.getBody());
        verify(addressRepository).getReferenceById(10);

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(captor.capture());
        assertSame(address, captor.getValue().getAddress());
    }

    @Test
    void should_reject_create_person_with_non_person_address() {
        Address nonPersonAddress = new Address();
        nonPersonAddress.setId(20);
        nonPersonAddress.setOwnerType(AddressOwnerType.Supplier);

        when(personRequestMapper.toEntity(requestDtoWithAddress)).thenReturn(person);
        when(addressRepository.getReferenceById(10)).thenReturn(nonPersonAddress);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> personService.createPerson(requestDtoWithAddress));

        assertTrue(ex.getMessage().contains("Adresse"));
        verify(addressRepository).getReferenceById(10);
        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    void should_update_person_without_address() {
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(personResponseMapper.toDto(any(Person.class))).thenReturn(responseDto);

        ResponseEntity<PersonResponseDTO> response = personService.updatePerson(1L, requestDtoNoAddress);

        assertEquals(200, response.getStatusCode().value());
        assertSame(responseDto, response.getBody());
        verify(personRequestMapper).partialUpdate(person, requestDtoNoAddress);
        verify(addressRepository, never()).getReferenceById(anyInt());
        verify(personRepository).save(person);
        verify(personResponseMapper).toDto(person);
    }

    @Test
    void should_update_person_with_address() {
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(addressRepository.getReferenceById(10)).thenReturn(address);
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(personResponseMapper.toDto(any(Person.class))).thenReturn(responseDto);

        ResponseEntity<PersonResponseDTO> response = personService.updatePerson(1L, requestDtoWithAddress);

        assertEquals(200, response.getStatusCode().value());
        assertSame(responseDto, response.getBody());
        verify(personRequestMapper).partialUpdate(person, requestDtoWithAddress);
        verify(addressRepository).getReferenceById(10);

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(captor.capture());
        assertSame(address, captor.getValue().getAddress());
    }

    @Test
    void should_reject_update_person_with_non_person_address() {
        Address nonPersonAddress = new Address();
        nonPersonAddress.setId(20);
        nonPersonAddress.setOwnerType(AddressOwnerType.Supplier);

        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(addressRepository.getReferenceById(10)).thenReturn(nonPersonAddress);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> personService.updatePerson(1L, requestDtoWithAddress));

        assertTrue(ex.getMessage().contains("Adresse"));
        verify(addressRepository).getReferenceById(10);
        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    void should_return_true_when_person_exists() {
        when(personRepository.existsById(1L)).thenReturn(true);

        boolean result = personService.existsById(1L);

        assertEquals(true, result);
        verify(personRepository).existsById(1L);
    }
}
