package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.VatResponseDTO;
import de.hs_esslingen.besy.mappers.response.VatResponseMapper;
import de.hs_esslingen.besy.models.Vat;
import de.hs_esslingen.besy.repositories.VatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VatServiceTest {

    @Mock
    private VatRepository vatRepository;

    @Mock
    private VatResponseMapper vatResponseMapper;

    @InjectMocks
    private VatService vatService;

    private Vat vat;
    private VatResponseDTO vatResponseDTO;

    @BeforeEach
    void setUp() {
        vat = new Vat();
        vat.setValue(BigDecimal.valueOf(19));
        vat.setDescription("Standard VAT");

        vatResponseDTO = new VatResponseDTO(BigDecimal.valueOf(19), "Standard VAT");
    }

    @Test
    void should_get_all_vats() {
        List<Vat> vats = List.of(vat);
        List<VatResponseDTO> dtos = List.of(vatResponseDTO);

        when(vatRepository.findAll()).thenReturn(vats);
        when(vatResponseMapper.toDto(vats)).thenReturn(dtos);

        ResponseEntity<List<VatResponseDTO>> response = vatService.getAllVats();

        assertEquals(200, response.getStatusCode().value());
        assertSame(dtos, response.getBody());
        verify(vatRepository, times(1)).findAll();
        verify(vatResponseMapper, times(1)).toDto(vats);
    }
}
