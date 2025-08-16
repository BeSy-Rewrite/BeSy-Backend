package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.response.CurrencyResponseDTO;
import de.hs_esslingen.besy.services.CurrencyService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<List<CurrencyResponseDTO>> getAll() {
        return currencyService.getAllCurrencies();
    }
}
