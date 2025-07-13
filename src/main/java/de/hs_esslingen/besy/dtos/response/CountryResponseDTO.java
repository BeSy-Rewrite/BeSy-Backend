package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Country}
 */
@Value
public class CountryResponseDTO implements Serializable {
    String countryName;
    String countryGerman;
    String countryCode;
}