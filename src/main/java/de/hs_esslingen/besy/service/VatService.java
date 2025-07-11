package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.response.VatResponseDTO;
import de.hs_esslingen.besy.mapper.response.VatResponseMapper;
import de.hs_esslingen.besy.model.Vat;
import de.hs_esslingen.besy.repository.VatRepository;
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
