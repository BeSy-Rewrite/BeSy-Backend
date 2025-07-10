package de.hs_esslingen.besy.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.model.Country}
 */
@Value
public class CountryResponseDTO implements Serializable {
    String countryName;
    String countryGerman;
    String countryCode;
}