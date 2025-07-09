package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.model.Country;
import de.hs_esslingen.besy.repository.CountryRepository;
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

    private final CountryRepository countryRepository;

    @GetMapping
    public ResponseEntity<List<Country>> getAddressTypes() {
        return ResponseEntity.ok(countryRepository.findAll());
    }
}
