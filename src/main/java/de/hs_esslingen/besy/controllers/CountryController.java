package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.response.CountryResponseDTO;
import de.hs_esslingen.besy.services.CountryService;
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
