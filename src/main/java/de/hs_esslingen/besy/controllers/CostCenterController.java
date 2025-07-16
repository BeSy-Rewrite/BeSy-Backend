package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.request.CostCenterRequestDTO;
import de.hs_esslingen.besy.dtos.response.CostCenterResponseDTO;
import de.hs_esslingen.besy.services.CostCenterService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/cost_centers")
public class CostCenterController {

    private final CostCenterService costCenterService;

    @GetMapping
    public ResponseEntity<List<CostCenterResponseDTO>> getCostCenters() {
        return costCenterService.getAllCostCenters();
    }

    @PostMapping
    public ResponseEntity<CostCenterResponseDTO> createCostCenter(@RequestBody CostCenterRequestDTO costCenterRequest) {
        return costCenterService.createCostCenter(costCenterRequest);
    }
}
