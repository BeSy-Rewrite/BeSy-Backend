package de.hs_esslingen.besy.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hs_esslingen.besy.dtos.response.VatResponseDTO;
import de.hs_esslingen.besy.services.VatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(value = "${api.prefix}/vats", produces = "application/json")
public class VatController {

    private final VatService vatService;

    @GetMapping
    @Operation(description = "Der `value` einer VAT ist zugleich dessen ID.")
    @ApiResponse(responseCode = "200", description = "Liste der Mehrwertsteuersätze.")
    public ResponseEntity<List<VatResponseDTO>> getAllVats() {
        return vatService.getAllVats();
    }
}
