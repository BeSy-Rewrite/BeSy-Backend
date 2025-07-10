package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.CurrencyResponseDTO;
import de.hs_esslingen.besy.mapper.CurrencyResponseMapper;
import de.hs_esslingen.besy.model.Currency;
import de.hs_esslingen.besy.repository.CurrencyRepository;
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
