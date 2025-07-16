package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.PrimaryCostCenterResponseDTO;
import de.hs_esslingen.besy.mappers.response.PrimaryCostCenterResponseMapper;
import de.hs_esslingen.besy.repositories.PrimaryCostCenterRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PrimaryCostCenterService {

    private final PrimaryCostCenterRepository primaryCostCenterRepository;
    private final PrimaryCostCenterResponseMapper primaryCostCenterResponseMapper;

    public ResponseEntity<List<PrimaryCostCenterResponseDTO>> getAllPrimaryCostCenters() {
        List<PrimaryCostCenter> primaryCostCenters = primaryCostCenterRepository.findAll();
        List<PrimaryCostCenterResponseDTO> primaryCostCenterResponseDTOS = primaryCostCenterResponseMapper.toDto(primaryCostCenters);
        return ResponseEntity.ok(primaryCostCenterResponseDTOS);
    }
}
