package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.request.CostCenterRequestDTO;
import de.hs_esslingen.besy.dto.response.CostCenterResponseDTO;
import de.hs_esslingen.besy.mapper.request.CostCenterRequestMapper;
import de.hs_esslingen.besy.mapper.response.CostCenterResponseMapper;
import de.hs_esslingen.besy.model.CostCenter;
import de.hs_esslingen.besy.repository.CostCenterRepository;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
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
        if(costCenterRepository.existsById(requestDTO.getCostCenterId())) return ResponseEntity.status(HttpStatus.CONFLICT).build();
        CostCenter costCenter = costCenterRequestMapper.toEntity(requestDTO);
        costCenterRepository.save(costCenter);
        return ResponseEntity.status(HttpStatus.CREATED).body(costCenterResponseMapper.toDto(costCenter));
    }

}
