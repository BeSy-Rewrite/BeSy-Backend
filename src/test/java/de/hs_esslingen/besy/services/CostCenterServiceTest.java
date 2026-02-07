package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.CostCenterRequestDTO;
import de.hs_esslingen.besy.dtos.response.CostCenterResponseDTO;
import de.hs_esslingen.besy.mappers.request.CostCenterRequestMapper;
import de.hs_esslingen.besy.mappers.response.CostCenterResponseMapper;
import de.hs_esslingen.besy.models.CostCenter;
import de.hs_esslingen.besy.repositories.CostCenterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CostCenterServiceTest {

    @Mock
    private CostCenterRepository costCenterRepository;

    @Mock
    private CostCenterResponseMapper costCenterResponseMapper;

    @Mock
    private CostCenterRequestMapper costCenterRequestMapper;

    @InjectMocks
    private CostCenterService costCenterService;

    private CostCenterRequestDTO requestDto;
    private CostCenter costCenter;
    private CostCenterResponseDTO responseDto;

    @BeforeEach
    void setUp() {
        requestDto = new CostCenterRequestDTO(
                "CC-100",
                "Main Cost Center",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                "Comment"
        );
        costCenter = new CostCenter();
        costCenter.setId("CC-100");
        costCenter.setName("Main Cost Center");
        costCenter.setBeginDate(LocalDate.of(2025, 1, 1));
        costCenter.setEndDate(LocalDate.of(2025, 12, 31));
        costCenter.setComment("Comment");
        responseDto = new CostCenterResponseDTO(
                "CC-100",
                "Main Cost Center",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                "Comment"
        );
    }

    @Test
    void should_return_all_cost_centers_as_dtos() {
        List<CostCenter> entities = List.of(costCenter);
        List<CostCenterResponseDTO> dtos = List.of(responseDto);

        when(costCenterRepository.findAll()).thenReturn(entities);
        when(costCenterResponseMapper.toDto(entities)).thenReturn(dtos);

        ResponseEntity<List<CostCenterResponseDTO>> response = costCenterService.getAllCostCenters();

        assertSame(dtos, response.getBody());
        verify(costCenterRepository).findAll();
        verify(costCenterResponseMapper).toDto(entities);
        verify(costCenterRepository, never()).delete(any(CostCenter.class));
        verify(costCenterRepository, never()).deleteById(anyString());
        verify(costCenterRepository, never()).deleteAll();
    }

    @Test
    void should_return_conflict_when_cost_center_id_exists() {
        when(costCenterRepository.existsById(requestDto.getId())).thenReturn(true);

        ResponseEntity<CostCenterResponseDTO> response = costCenterService.createCostCenter(requestDto);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());
        verify(costCenterRepository).existsById(requestDto.getId());
        verify(costCenterRepository, never()).save(any(CostCenter.class));
        verifyNoInteractions(costCenterRequestMapper);
        verifyNoInteractions(costCenterResponseMapper);
        verify(costCenterRepository, never()).delete(any(CostCenter.class));
        verify(costCenterRepository, never()).deleteById(anyString());
        verify(costCenterRepository, never()).deleteAll();
    }

    @Test
    void should_create_cost_center_and_return_created_dto() {
        when(costCenterRepository.existsById(requestDto.getId())).thenReturn(false);
        when(costCenterRequestMapper.toEntity(requestDto)).thenReturn(costCenter);
        when(costCenterRepository.save(costCenter)).thenReturn(costCenter);
        when(costCenterResponseMapper.toDto(costCenter)).thenReturn(responseDto);

        ResponseEntity<CostCenterResponseDTO> response = costCenterService.createCostCenter(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(responseDto, response.getBody());
        verify(costCenterRepository).existsById(requestDto.getId());
        verify(costCenterRequestMapper).toEntity(requestDto);
        verify(costCenterRepository).save(costCenter);
        verify(costCenterResponseMapper).toDto(costCenter);
        verify(costCenterRepository, never()).delete(any(CostCenter.class));
        verify(costCenterRepository, never()).deleteById(anyString());
        verify(costCenterRepository, never()).deleteAll();
    }

    @Test
    void should_return_true_when_cost_center_exists() {
        when(costCenterRepository.existsById("CC-200")).thenReturn(true);

        boolean result = costCenterService.existsById("CC-200");

        assertEquals(true, result);
        verify(costCenterRepository).existsById("CC-200");
        verify(costCenterRepository, never()).delete(any(CostCenter.class));
        verify(costCenterRepository, never()).deleteById(anyString());
        verify(costCenterRepository, never()).deleteAll();
    }

    @Test
    void should_return_false_when_cost_center_does_not_exist() {
        when(costCenterRepository.existsById("CC-201")).thenReturn(false);

        boolean result = costCenterService.existsById("CC-201");

        assertEquals(false, result);
        verify(costCenterRepository).existsById("CC-201");
        verify(costCenterRepository, never()).delete(any(CostCenter.class));
        verify(costCenterRepository, never()).deleteById(anyString());
        verify(costCenterRepository, never()).deleteAll();
    }
}
