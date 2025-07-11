package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.response.CountryResponseDTO;
import de.hs_esslingen.besy.mapper.response.CountryResponseMapper;
import de.hs_esslingen.besy.model.Country;
import de.hs_esslingen.besy.repository.CountryRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;
    private final CountryResponseMapper countryResponseMapper;

    public ResponseEntity<List<CountryResponseDTO>> getAllCountries() {
        List<Country> countries = countryRepository.findAll();
        List<CountryResponseDTO> countryResponseDTOS = countryResponseMapper.toDto(countries);
        return ResponseEntity.ok(countryResponseDTOS);
    }
}
