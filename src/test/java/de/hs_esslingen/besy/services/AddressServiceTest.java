package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.AddressRequestDTO;
import de.hs_esslingen.besy.dtos.response.AddressResponseDTO;
import de.hs_esslingen.besy.enums.AddressOwnerType;
import de.hs_esslingen.besy.enums.Gender;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.request.AddressRequestMapper;
import de.hs_esslingen.besy.mappers.response.AddressResponseMapper;
import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.models.Person;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.repositories.AddressRepository;
import de.hs_esslingen.besy.repositories.PersonRepository;
import de.hs_esslingen.besy.repositories.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private AddressRequestMapper addressRequestMapper;

    @Mock
    private AddressResponseMapper addressResponseMapper;

    @Mock
    private SupplierService supplierService;

    @InjectMocks
    private AddressService addressService;

    @Test
    void should_create_and_return_address_with_owner_type_supplier() {
        AddressRequestDTO dto = new AddressRequestDTO("B1", "Main St", "10", "Town", "12345", "County", "Country", "Comment");
        Address mapped = new Address();
        Address persisted = new Address();
        persisted.setId(1);
        persisted.setOwnerType(AddressOwnerType.Supplier);
        AddressResponseDTO responseDto = new AddressResponseDTO(1, "B1", "Main St", "10", "Town", "12345", "County", "Country", "Comment");

        when(addressRequestMapper.toEntity(dto)).thenReturn(mapped);
        when(addressRepository.save(any(Address.class))).thenReturn(persisted);
        when(addressResponseMapper.toDto(persisted)).thenReturn(responseDto);

        ResponseEntity<AddressResponseDTO> response = addressService.createAddress(dto, AddressOwnerType.Supplier);

        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(captor.capture());
        assertEquals(AddressOwnerType.Supplier, captor.getValue().getOwnerType());
        assertSame(responseDto, response.getBody());
    }

    @Test
    void should_create_and_return_address_with_owner_type_person() {
        AddressRequestDTO dto = new AddressRequestDTO("B2", "Second St", "20", "Town", "54321", "County", "Country", "Comment");
        Address mapped = new Address();
        Address persisted = new Address();
        persisted.setId(2);
        persisted.setOwnerType(AddressOwnerType.Person);
        AddressResponseDTO responseDto = new AddressResponseDTO(2, "B2", "Second St", "20", "Town", "54321", "County", "Country", "Comment");

        when(addressRequestMapper.toEntity(dto)).thenReturn(mapped);
        when(addressRepository.save(any(Address.class))).thenReturn(persisted);
        when(addressResponseMapper.toDto(persisted)).thenReturn(responseDto);

        ResponseEntity<AddressResponseDTO> response = addressService.createAddress(dto, AddressOwnerType.Person);

        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(captor.capture());
        assertEquals(AddressOwnerType.Person, captor.getValue().getOwnerType());
        assertSame(responseDto, response.getBody());
    }

    @Test
    void should_map_dto_to_entity_to_dto_correctly() {
        AddressRequestDTO dto = new AddressRequestDTO("B3", "Third St", "30", "Town", "11111", "County", "Country", "Comment");
        Address mapped = new Address();
        Address persisted = new Address();
        persisted.setId(3);
        persisted.setOwnerType(AddressOwnerType.Supplier);
        AddressResponseDTO responseDto = new AddressResponseDTO(3, "B3", "Third St", "30", "Town", "11111", "County", "Country", "Comment");

        when(addressRequestMapper.toEntity(dto)).thenReturn(mapped);
        when(addressRepository.save(mapped)).thenReturn(persisted);
        when(addressResponseMapper.toDto(persisted)).thenReturn(responseDto);

        ResponseEntity<AddressResponseDTO> response = addressService.createAddress(dto, AddressOwnerType.Supplier);

        InOrder inOrder = inOrder(addressRequestMapper, addressRepository, addressResponseMapper);
        inOrder.verify(addressRequestMapper).toEntity(dto);
        inOrder.verify(addressRepository).save(mapped);
        inOrder.verify(addressResponseMapper).toDto(persisted);
        assertSame(responseDto, response.getBody());
    }

    @Test
    void should_return_only_addresses_with_owner_type_supplier() {
        Address supplierAddress = new Address();
        supplierAddress.setOwnerType(AddressOwnerType.Supplier);
        List<Address> addresses = List.of(supplierAddress);
        List<AddressResponseDTO> responseDtos = List.of(
                new AddressResponseDTO(1, "B1", "Main St", "10", "Town", "12345", "County", "Country", "Comment")
        );

        when(addressRepository.getAddressByOwnerType(AddressOwnerType.Supplier)).thenReturn(addresses);
        when(addressResponseMapper.toDto(addresses)).thenReturn(responseDtos);

        ResponseEntity<List<AddressResponseDTO>> response = addressService.getSupplierAddresses();

        assertSame(responseDtos, response.getBody());
        verify(addressRepository).getAddressByOwnerType(AddressOwnerType.Supplier);
        verify(addressResponseMapper).toDto(addresses);
    }

    @Test
    void should_call_repository_to_get_supplier_addresses_by_owner_type() {
        when(addressRepository.getAddressByOwnerType(AddressOwnerType.Supplier)).thenReturn(List.of());
        when(addressResponseMapper.toDto(List.of())).thenReturn(List.of());

        addressService.getSupplierAddresses();

        verify(addressRepository).getAddressByOwnerType(AddressOwnerType.Supplier);
    }

    @Test
    void should_return_only_addresses_with_owner_type_person() {
        Address personAddress = new Address();
        personAddress.setOwnerType(AddressOwnerType.Person);
        List<Address> addresses = List.of(personAddress);
        List<AddressResponseDTO> responseDtos = List.of(
                new AddressResponseDTO(2, "B2", "Second St", "20", "Town", "54321", "County", "Country", "Comment")
        );

        when(addressRepository.getAddressByOwnerType(AddressOwnerType.Person)).thenReturn(addresses);
        when(addressResponseMapper.toDto(addresses)).thenReturn(responseDtos);

        ResponseEntity<List<AddressResponseDTO>> response = addressService.getPersonAddresses();

        assertSame(responseDtos, response.getBody());
        verify(addressRepository).getAddressByOwnerType(AddressOwnerType.Person);
        verify(addressResponseMapper).toDto(addresses);
    }

    @Test
    void should_call_repository_to_get_person_addresses_by_owner_type() {
        when(addressRepository.getAddressByOwnerType(AddressOwnerType.Person)).thenReturn(List.of());
        when(addressResponseMapper.toDto(List.of())).thenReturn(List.of());

        addressService.getPersonAddresses();

        verify(addressRepository).getAddressByOwnerType(AddressOwnerType.Person);
    }

    @Test
    void should_return_supplier_address_when_exists_and_is_supplier_type() {
        Integer supplierId = 10;
        Integer addressId = 100;
        Supplier supplier = new Supplier();
        supplier.setAddressId(addressId);
        Address address = new Address();
        address.setOwnerType(AddressOwnerType.Supplier);
        supplier.setAddress(address);
        AddressResponseDTO responseDto = new AddressResponseDTO(100, "B1", "Main St", "10", "Town", "12345", "County", "Country", "Comment");

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(addressRepository.existsById(addressId)).thenReturn(true);
        when(addressResponseMapper.toDto(address)).thenReturn(responseDto);

        ResponseEntity<AddressResponseDTO> response = addressService.getAddressOfSupplier(supplierId);

        assertSame(responseDto, response.getBody());
        verify(addressRepository).existsById(addressId);
        verify(addressResponseMapper).toDto(address);
    }

    @Test
    void should_throw_not_found_when_supplier_has_no_address() {
        Integer supplierId = 11;
        Supplier supplier = new Supplier();
        supplier.setAddressId(null);

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));

        assertThrows(NotFoundException.class, () -> addressService.getAddressOfSupplier(supplierId));

        verify(addressRepository, never()).existsById(any());
        verify(addressResponseMapper, never()).toDto(any(Address.class));
    }

    @Test
    void should_throw_not_found_when_supplier_address_does_not_exist() {
        Integer supplierId = 12;
        Integer addressId = 120;
        Supplier supplier = new Supplier();
        supplier.setAddressId(addressId);

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(addressRepository.existsById(addressId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> addressService.getAddressOfSupplier(supplierId));

        verify(addressResponseMapper, never()).toDto(any(Address.class));
    }

    @Test
    void should_not_allow_returning_person_address_for_supplier() {
        Integer supplierId = 13;
        Integer addressId = 130;
        Supplier supplier = new Supplier();
        supplier.setAddressId(addressId);
        Address address = new Address();
        address.setOwnerType(AddressOwnerType.Person);
        supplier.setAddress(address);

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(addressRepository.existsById(addressId)).thenReturn(true);

        assertThrows(NotFoundException.class, () -> addressService.getAddressOfSupplier(supplierId));

        verify(addressResponseMapper, never()).toDto(any(Address.class));
    }

    @Test
    void should_return_person_address_when_exists_and_is_person_type() {
        Long personId = 20L;
        Integer addressId = 200;
        Person person = new Person();
        person.setGender(Gender.m);
        person.setAddressId(addressId);
        Address address = new Address();
        address.setOwnerType(AddressOwnerType.Person);
        person.setAddress(address);
        AddressResponseDTO responseDto = new AddressResponseDTO(200, "B2", "Second St", "20", "Town", "54321", "County", "Country", "Comment");

        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(addressRepository.existsById(addressId)).thenReturn(true);
        when(addressResponseMapper.toDto(address)).thenReturn(responseDto);

        ResponseEntity<AddressResponseDTO> response = addressService.getAddressOfPerson(personId);

        assertSame(responseDto, response.getBody());
        verify(addressRepository).existsById(addressId);
        verify(addressResponseMapper).toDto(address);
    }

    @Test
    void should_throw_not_found_when_person_has_no_address() {
        Long personId = 21L;
        Person person = new Person();
        person.setGender(Gender.m);
        person.setAddressId(null);

        when(personRepository.findById(personId)).thenReturn(Optional.of(person));

        assertThrows(NotFoundException.class, () -> addressService.getAddressOfPerson(personId));

        verify(addressRepository, never()).existsById(any());
        verify(addressResponseMapper, never()).toDto(any(Address.class));
    }

    @Test
    void should_throw_not_found_when_person_address_does_not_exist() {
        Long personId = 22L;
        Integer addressId = 220;
        Person person = new Person();
        person.setGender(Gender.m);
        person.setAddressId(addressId);

        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(addressRepository.existsById(addressId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> addressService.getAddressOfPerson(personId));

        verify(addressResponseMapper, never()).toDto(any(Address.class));
    }

    @Test
    void should_not_allow_returning_supplier_address_for_person() {
        Long personId = 23L;
        Integer addressId = 230;
        Person person = new Person();
        person.setGender(Gender.m);
        person.setAddressId(addressId);
        Address address = new Address();
        address.setOwnerType(AddressOwnerType.Supplier);
        person.setAddress(address);

        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(addressRepository.existsById(addressId)).thenReturn(true);

        assertThrows(NotFoundException.class, () -> addressService.getAddressOfPerson(personId));

        verify(addressResponseMapper, never()).toDto(any(Address.class));
    }

    @Test
    void should_return_true_when_address_exists() {
        when(addressRepository.existsById(1)).thenReturn(true);

        boolean result = addressService.existsById(1);

        assertEquals(true, result);
        verify(addressRepository).existsById(1);
    }

    @Test
    void should_return_false_when_address_does_not_exist() {
        when(addressRepository.existsById(2)).thenReturn(false);

        boolean result = addressService.existsById(2);

        assertEquals(false, result);
        verify(addressRepository).existsById(2);
    }
}
