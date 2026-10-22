package de.hs_esslingen.besy.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hs_esslingen.besy.dtos.response.CurrencyResponseDTO;
import de.hs_esslingen.besy.services.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(value = "${api.prefix}/currencies", produces = "application/json")
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    @Operation(description = "Gibt alle verfügbaren Währungen zurück.")
    @ApiResponse(responseCode = "200", description = "Liste der Währungen.")
    public ResponseEntity<List<CurrencyResponseDTO>> getAllCurrencies() {
        return currencyService.getAllCurrencies();
    }

    @GetMapping("/{code}")
    @Operation(description = "Gibt eine Währung anhand ihres ISO-Codes zurück.")
    @ApiResponse(responseCode = "200", description = "Währung gefunden")
    @ApiResponse(responseCode = "404", description = "Währung nicht gefunden")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler")
    public ResponseEntity<CurrencyResponseDTO> getCurrencyByCode(
            @Parameter(description = "Der ISO-Code der abzurufenden Währung") @PathVariable("code") String code) {
        return currencyService.getCurrencyByCode(code);
    }
}
