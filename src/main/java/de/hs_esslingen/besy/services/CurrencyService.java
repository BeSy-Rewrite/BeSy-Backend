package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.CurrencyResponseDTO;
import de.hs_esslingen.besy.mappers.response.CurrencyResponseMapper;
import de.hs_esslingen.besy.models.Currency;
import de.hs_esslingen.besy.repositories.CurrencyRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/currencies")
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyResponseMapper currencyResponseMapper;

    public ResponseEntity<List<CurrencyResponseDTO>> getAllCurrencies() {
        List<Currency> currencies = currencyRepository.findAll();
        List<CurrencyResponseDTO> currencyResponseDTOS = currencyResponseMapper.toDto(currencies);
        return ResponseEntity.ok(currencyResponseDTOS);
    }
}
