package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.CostCenterResponseDTO;
import de.hs_esslingen.besy.mapper.CostCenterResponseMapper;
import de.hs_esslingen.besy.model.CostCenter;
import de.hs_esslingen.besy.repository.CostCenterRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CostCenterService {

    private final CostCenterRepository costCenterRepository;
    private final CostCenterResponseMapper costCenterResponseMapper;

    public ResponseEntity<List<CostCenterResponseDTO>> getAllCostCenters() {
        List<CostCenter> costCenters = costCenterRepository.findAll();
        List<CostCenterResponseDTO> costCenterResponseDTOS = costCenterResponseMapper.toDto(costCenters);
        return ResponseEntity.ok(costCenterResponseDTOS);
    }
}
