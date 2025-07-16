package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.VatResponseDTO;
import de.hs_esslingen.besy.mappers.response.VatResponseMapper;
import de.hs_esslingen.besy.models.Vat;
import de.hs_esslingen.besy.repositories.VatRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class VatService {

    private final VatRepository vatRepository;
    private final VatResponseMapper vatResponseMapper;

    public ResponseEntity<List<VatResponseDTO>> getAllVats() {
        List<Vat> vats = vatRepository.findAll();
        List<VatResponseDTO> vatResponseDTOS = vatResponseMapper.toDto(vats);
        return ResponseEntity.ok(vatResponseDTOS);
    }
}
