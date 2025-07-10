package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.dto.CostCenterResponseDTO;
import de.hs_esslingen.besy.service.CostCenterService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
