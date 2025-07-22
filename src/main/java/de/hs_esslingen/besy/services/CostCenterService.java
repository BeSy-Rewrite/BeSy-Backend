package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.CostCenterRequestDTO;
import de.hs_esslingen.besy.dtos.response.CostCenterResponseDTO;
import de.hs_esslingen.besy.mappers.request.CostCenterRequestMapper;
import de.hs_esslingen.besy.mappers.response.CostCenterResponseMapper;
import de.hs_esslingen.besy.models.CostCenter;
import de.hs_esslingen.besy.repositories.CostCenterRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CostCenterService {

    private final CostCenterRepository costCenterRepository;
    private final CostCenterResponseMapper costCenterResponseMapper;
    private final CostCenterRequestMapper costCenterRequestMapper;

    public ResponseEntity<List<CostCenterResponseDTO>> getAllCostCenters() {
        List<CostCenter> costCenters = costCenterRepository.findAll();
        List<CostCenterResponseDTO> costCenterResponseDTOS = costCenterResponseMapper.toDto(costCenters);
        return ResponseEntity.ok(costCenterResponseDTOS);
    }

    public ResponseEntity<CostCenterResponseDTO> createCostCenter(CostCenterRequestDTO requestDTO) {
        if(costCenterRepository.existsById(requestDTO.getId())) return ResponseEntity.status(HttpStatus.CONFLICT).build();
        CostCenter costCenter = costCenterRequestMapper.toEntity(requestDTO);
        costCenterRepository.save(costCenter);
        return ResponseEntity.status(HttpStatus.CREATED).body(costCenterResponseMapper.toDto(costCenter));
    }

}
