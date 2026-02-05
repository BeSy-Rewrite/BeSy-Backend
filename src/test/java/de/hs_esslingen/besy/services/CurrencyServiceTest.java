package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.CurrencyResponseDTO;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.response.CurrencyResponseMapper;
import de.hs_esslingen.besy.models.Currency;
import de.hs_esslingen.besy.repositories.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private CurrencyResponseMapper currencyResponseMapper;

    @InjectMocks
    private CurrencyService currencyService;

    private Currency currency;
    private CurrencyResponseDTO responseDto;

    @BeforeEach
    void setUp() {
        currency = new Currency();
        currency.setCode("EUR");
        currency.setName("Euro");
        responseDto = new CurrencyResponseDTO("EUR", "Euro");
    }

    @Test
    void should_return_all_currencies_as_dtos() {
        List<Currency> currencies = List.of(currency);
        List<CurrencyResponseDTO> dtos = List.of(responseDto);

        when(currencyRepository.findAll()).thenReturn(currencies);
        when(currencyResponseMapper.toDto(currencies)).thenReturn(dtos);

        ResponseEntity<List<CurrencyResponseDTO>> response = currencyService.getAllCurrencies();

        assertSame(dtos, response.getBody());
        verify(currencyRepository).findAll();
        verify(currencyResponseMapper).toDto(currencies);
        verify(currencyRepository, never()).save(any(Currency.class));
        verify(currencyRepository, never()).delete(any(Currency.class));
        verify(currencyRepository, never()).deleteById(anyString());
        verify(currencyRepository, never()).deleteAll();
    }

    @Test
    void should_return_currency_dto_when_currency_exists() {
        when(currencyRepository.findById("EUR")).thenReturn(Optional.of(currency));
        when(currencyResponseMapper.toDto(currency)).thenReturn(responseDto);

        ResponseEntity<CurrencyResponseDTO> response = currencyService.getCurrencyByCode("EUR");

        assertSame(responseDto, response.getBody());
        verify(currencyRepository).findById("EUR");
        verify(currencyResponseMapper).toDto(currency);
        verify(currencyRepository, never()).save(any(Currency.class));
        verify(currencyRepository, never()).delete(any(Currency.class));
        verify(currencyRepository, never()).deleteById(anyString());
        verify(currencyRepository, never()).deleteAll();
    }

    @Test
    void should_throw_not_found_when_currency_does_not_exist() {
        when(currencyRepository.findById("USD")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> currencyService.getCurrencyByCode("USD"));

        verify(currencyRepository).findById("USD");
        verify(currencyRepository, never()).save(any(Currency.class));
        verify(currencyRepository, never()).delete(any(Currency.class));
        verify(currencyRepository, never()).deleteById(anyString());
        verify(currencyRepository, never()).deleteAll();
    }
}
