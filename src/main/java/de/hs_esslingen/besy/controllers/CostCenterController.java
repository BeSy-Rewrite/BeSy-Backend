package de.hs_esslingen.besy.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hs_esslingen.besy.dtos.request.CostCenterRequestDTO;
import de.hs_esslingen.besy.dtos.request.UpdateCostCenterDTO;
import de.hs_esslingen.besy.dtos.response.CostCenterResponseDTO;
import de.hs_esslingen.besy.mappers.response.CostCenterResponseMapper;
import de.hs_esslingen.besy.services.CostCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(value = "${api.prefix}/cost_centers", produces = "application/json")
public class CostCenterController {

    private final CostCenterService costCenterService;
    private final CostCenterResponseMapper costCenterResponseMapper;

    @GetMapping
    @Operation(description = "Gibt alle Kostenstellen zurück.")
    @ApiResponse(responseCode = "200", description = "Liste der Kostenstellen.")
    public ResponseEntity<List<CostCenterResponseDTO>> getAllCostCenters() {
        return costCenterService.getAllCostCenters();
    }

    @PostMapping
    @Operation(description = "Erstellt eine neue Kostenstelle.")
    @ApiResponse(responseCode = "201", description = "Kostenstelle erfolgreich erstellt.")
    @ApiResponse(responseCode = "409", description = "Kostenstelle mit diesem Namen existiert bereits.")
    public ResponseEntity<CostCenterResponseDTO> createCostCenter(@RequestBody CostCenterRequestDTO costCenterRequest) {
        return costCenterService.createCostCenter(costCenterRequest);
    }
}
