package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.CustomerIdRequestDTO;
import de.hs_esslingen.besy.dtos.response.CustomerIdResponseDTO;
import de.hs_esslingen.besy.exceptions.EntityAlreadyExistsException;
import de.hs_esslingen.besy.mappers.request.CustomerIdRequestMapper;
import de.hs_esslingen.besy.mappers.response.CustomerIdResponseMapper;
import de.hs_esslingen.besy.models.CustomerId;
import de.hs_esslingen.besy.models.CustomerIdId;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.repositories.CustomerIdRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerIdServiceTest {

    @Mock
    private CustomerIdRepository customerIdRepository;

    @Mock
    private CustomerIdResponseMapper customerIdResponseMapper;

    @Mock
    private CustomerIdRequestMapper customerIdRequestMapper;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private CustomerIdService customerIdService;

    private CustomerIdRequestDTO requestDto;
    private CustomerId customerId;
    private CustomerIdResponseDTO responseDto;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        requestDto = new CustomerIdRequestDTO("CUST-1", "Comment");
        customerId = new CustomerId();
        customerId.setCustomerId("CUST-1");
        customerId.setComment("Comment");
        responseDto = new CustomerIdResponseDTO("CUST-1", "Comment");
        supplier = new Supplier();
    }

    @Test
    void should_return_all_customer_ids_for_supplier() {
        Integer supplierId = 1;
        List<CustomerId> entities = List.of(customerId);
        List<CustomerIdResponseDTO> dtos = List.of(responseDto);

        when(customerIdRepository.findAllBySupplierId(supplierId)).thenReturn(entities);
        when(customerIdResponseMapper.toDto(entities)).thenReturn(dtos);

        ResponseEntity<List<CustomerIdResponseDTO>> response = customerIdService.getAllCustomerIds(supplierId);

        assertSame(dtos, response.getBody());
        verify(customerIdRepository).findAllBySupplierId(supplierId);
        verify(customerIdResponseMapper).toDto(entities);
        verify(customerIdRepository, never()).save(any(CustomerId.class));
        verify(customerIdRepository, never()).delete(any(CustomerId.class));
        verify(customerIdRepository, never()).deleteById(any(CustomerIdId.class));
        verify(customerIdRepository, never()).deleteAll();
    }

    @Test
    void should_create_customer_id_when_not_exists() {
        Integer supplierId = 2;
        CustomerIdId expectedId = new CustomerIdId("CUST-1", supplierId);

        when(customerIdRepository.existsById(expectedId)).thenReturn(false);
        when(supplierRepository.getReferenceById(supplierId)).thenReturn(supplier);
        when(customerIdRequestMapper.toEntity(requestDto)).thenReturn(customerId);
        when(customerIdRepository.save(customerId)).thenReturn(customerId);
        when(customerIdResponseMapper.toDto(customerId)).thenReturn(responseDto);

        ResponseEntity<CustomerIdResponseDTO> response = customerIdService.createCustomerId(supplierId, requestDto);

        assertSame(responseDto, response.getBody());
        verify(customerIdRepository).existsById(expectedId);
        verify(supplierRepository).getReferenceById(supplierId);
        verify(customerIdRequestMapper).toEntity(requestDto);
        verify(customerIdRepository).save(customerId);
        verify(customerIdResponseMapper).toDto(customerId);

        ArgumentCaptor<CustomerId> captor = ArgumentCaptor.forClass(CustomerId.class);
        verify(customerIdRepository).save(captor.capture());
        CustomerId saved = captor.getValue();
        assertSame(supplier, saved.getSupplier());
        assertEquals(expectedId, saved.getId());

        verify(customerIdRepository, never()).delete(any(CustomerId.class));
        verify(customerIdRepository, never()).deleteById(any(CustomerIdId.class));
        verify(customerIdRepository, never()).deleteAll();
    }

    @Test
    void should_throw_entity_already_exists_when_customer_id_exists() {
        Integer supplierId = 3;
        CustomerIdId expectedId = new CustomerIdId("CUST-1", supplierId);

        when(customerIdRepository.existsById(expectedId)).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, () -> customerIdService.createCustomerId(supplierId, requestDto));

        verify(customerIdRepository).existsById(expectedId);
        verifyNoInteractions(customerIdRequestMapper);
        verifyNoInteractions(customerIdResponseMapper);
        verifyNoInteractions(supplierRepository);
        verify(customerIdRepository, never()).save(any(CustomerId.class));
        verify(customerIdRepository, never()).delete(any(CustomerId.class));
        verify(customerIdRepository, never()).deleteById(any(CustomerIdId.class));
        verify(customerIdRepository, never()).deleteAll();
    }

    @Test
    void should_create_customer_id_with_generated_id_when_customer_id_is_null() {
        Integer supplierId = 4;
        CustomerIdRequestDTO requestDtoWithNullId = new CustomerIdRequestDTO(null, "Comment");

        when(customerIdRepository.existsById(any(CustomerIdId.class))).thenReturn(false);
        when(supplierRepository.getReferenceById(supplierId)).thenReturn(supplier);
        when(customerIdRequestMapper.toEntity(requestDtoWithNullId)).thenReturn(customerId);
        when(customerIdRepository.save(customerId)).thenReturn(customerId);
        when(customerIdResponseMapper.toDto(customerId)).thenReturn(responseDto);

        ResponseEntity<CustomerIdResponseDTO> response = customerIdService.createCustomerId(supplierId, requestDtoWithNullId);

        assertSame(responseDto, response.getBody());

        ArgumentCaptor<CustomerIdId> idCaptor = ArgumentCaptor.forClass(CustomerIdId.class);
        verify(customerIdRepository).existsById(idCaptor.capture());
        CustomerIdId capturedId = idCaptor.getValue();
        assertNotNull(capturedId.getCustomerId());
        assertEquals(supplierId, capturedId.getSupplierId());

        ArgumentCaptor<CustomerId> entityCaptor = ArgumentCaptor.forClass(CustomerId.class);
        verify(customerIdRepository).save(entityCaptor.capture());
        CustomerId savedEntity = entityCaptor.getValue();
        assertNotNull(savedEntity.getId());
        assertNotNull(savedEntity.getId().getCustomerId());
        assertEquals(supplierId, savedEntity.getId().getSupplierId());
    }
}
