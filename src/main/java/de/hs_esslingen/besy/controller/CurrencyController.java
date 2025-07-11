package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.dto.response.PrimaryCostCenterResponseDTO;
import de.hs_esslingen.besy.service.PrimaryCostCenterService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/primary_cost_center")
public class CurrencyController {

    private final PrimaryCostCenterService primaryCostCenterService;

    @GetMapping
    public ResponseEntity<List<PrimaryCostCenterResponseDTO>> getAllCurrencies() {
        return primaryCostCenterService.getAllPrimaryCostCenters();
    }
}
