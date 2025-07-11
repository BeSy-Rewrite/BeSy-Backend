package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.dto.response.CurrencyResponseDTO;
import de.hs_esslingen.besy.dto.response.PrimaryCostCenterResponseDTO;
import de.hs_esslingen.besy.service.CurrencyService;
import de.hs_esslingen.besy.service.PrimaryCostCenterService;
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
    public ResponseEntity<List<CurrencyResponseDTO>> getAllCurrencies() {
        return currencyService.getAllCurrencies();
    }
}
