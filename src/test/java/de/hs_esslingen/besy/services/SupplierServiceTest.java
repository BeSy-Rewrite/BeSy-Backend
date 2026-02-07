package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.AddressRequestDTO;
import de.hs_esslingen.besy.dtos.request.SupplierRequestDTO;
import de.hs_esslingen.besy.dtos.response.AddressResponseDTO;
import de.hs_esslingen.besy.dtos.response.CreateSupplierResponseDTO;
import de.hs_esslingen.besy.dtos.response.SupplierResponseDTO;
import de.hs_esslingen.besy.enums.AddressOwnerType;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.request.AddressRequestMapper;
import de.hs_esslingen.besy.mappers.request.SupplierRequestMapper;
import de.hs_esslingen.besy.mappers.response.CreateSupplierResponseMapper;
import de.hs_esslingen.besy.mappers.response.SupplierResponseMapper;
import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.repositories.SupplierRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierRequestMapper supplierRequestMapper;

    @Mock
    private SupplierResponseMapper supplierResponseMapper;

    @Mock
    private CreateSupplierResponseMapper createSupplierResponseMapper;

    @Mock
    private AddressRequestMapper addressRequestMapper;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier supplier;
    private Address address;
    private SupplierRequestDTO requestDto;
    private AddressRequestDTO addressRequestDto;
    private SupplierResponseDTO responseDto;
    private CreateSupplierResponseDTO createResponseDto;
    private AddressResponseDTO addressResponseDto;

    @BeforeEach
    void setUp() {
        address = new Address();
        address.setId(10);
        address.setOwnerType(AddressOwnerType.Supplier);

        supplier = new Supplier();
        supplier.setId(1);
        supplier.setName("Supplier GmbH");
        supplier.setEmail("supplier@example.com");
        supplier.setAddress(address);

        addressRequestDto = new AddressRequestDTO(
                "HQ",
                "Main St",
                "10",
                "Town",
                "12345",
                "County",
                "Country",
                "Comment"
        );

        requestDto = new SupplierRequestDTO(
                "Supplier GmbH",
                "123",
                "456",
                "supplier@example.com",
                "Comment",
                "https://example.com",
                "VAT-1",
                false,
                addressRequestDto
        );

        responseDto = new SupplierResponseDTO(
                1,
                "Supplier GmbH",
                "123",
                "456",
                "supplier@example.com",
                "Comment",
                "https://example.com",
                "VAT-1",
                false,
                null
        );

        addressResponseDto = new AddressResponseDTO(
                10,
                "HQ",
                "Main St",
                "10",
                "Town",
                "12345",
                "County",
                "Country",
                "Comment"
        );

        createResponseDto = new CreateSupplierResponseDTO(
                1,
                "Supplier GmbH",
                "123",
                "456",
                "supplier@example.com",
                "Comment",
                "https://example.com",
                "VAT-1",
                false,
                null,
                addressResponseDto
        );
    }

    @Test
    void should_get_all_suppliers() {
        List<Supplier> suppliers = List.of(supplier);
        List<SupplierResponseDTO> dtos = List.of(responseDto);

        when(supplierRepository.findAll()).thenReturn(suppliers);
        when(supplierResponseMapper.toDto(suppliers)).thenReturn(dtos);

        ResponseEntity<List<SupplierResponseDTO>> response = supplierService.getAllSuppliers();

        assertEquals(200, response.getStatusCode().value());
        assertSame(dtos, response.getBody());
        verify(supplierRepository).findAll();
        verify(supplierResponseMapper).toDto(suppliers);
    }

    @Test
    void should_get_supplier_by_id_when_exists() {
        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));
        when(supplierResponseMapper.toDto(supplier)).thenReturn(responseDto);

        ResponseEntity<SupplierResponseDTO> response = supplierService.getSupplierById(1);

        assertEquals(200, response.getStatusCode().value());
        assertSame(responseDto, response.getBody());
        verify(supplierRepository).findById(1);
        verify(supplierResponseMapper).toDto(supplier);
    }

    @Test
    void should_throw_not_found_when_supplier_missing() {
        when(supplierRepository.findById(1)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> supplierService.getSupplierById(1));

        assertEquals(true, ex.getMessage().contains("1"));
        verify(supplierRepository).findById(1);
    }

    @Test
    void should_create_supplier_and_set_address_owner_type() {
        when(supplierRequestMapper.toEntity(requestDto)).thenReturn(supplier);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(createSupplierResponseMapper.toDto(any(Supplier.class))).thenReturn(createResponseDto);

        ResponseEntity<CreateSupplierResponseDTO> response = supplierService.createSupplier(requestDto);

        assertEquals(200, response.getStatusCode().value());
        assertSame(createResponseDto, response.getBody());
        verify(supplierRequestMapper).toEntity(requestDto);

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository, times(1)).save(captor.capture());
        Supplier saved = captor.getValue();
        assertSame(AddressOwnerType.Supplier, saved.getAddress().getOwnerType());
        verify(createSupplierResponseMapper).toDto(saved);
    }

    @Test
    void should_update_supplier_and_update_address() {
        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(createSupplierResponseMapper.toDto(any(Supplier.class))).thenReturn(createResponseDto);

        ResponseEntity<CreateSupplierResponseDTO> response = supplierService.updateSupplier(1, requestDto);

        assertEquals(200, response.getStatusCode().value());
        assertSame(createResponseDto, response.getBody());
        verify(supplierRequestMapper).partialUpdate(supplier, requestDto);
        verify(addressRequestMapper).partialUpdate(address, requestDto.getAddress());

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(captor.capture());
        assertSame(AddressOwnerType.Supplier, captor.getValue().getAddress().getOwnerType());
    }

    @Test
    void should_exists_supplier_by_id() {
        when(supplierRepository.existsById(1)).thenReturn(true);

        boolean result = supplierService.existsSupplierById(1);

        assertEquals(true, result);
        verify(supplierRepository).existsById(1);
    }
}
