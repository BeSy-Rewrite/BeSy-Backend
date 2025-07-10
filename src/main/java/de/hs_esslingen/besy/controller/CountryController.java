package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.dto.CountryResponseDTO;
import de.hs_esslingen.besy.service.CountryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/countries")
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    public ResponseEntity<List<CountryResponseDTO>> getAddressTypes() {
        return countryService.getAllCountries();
    }
}
