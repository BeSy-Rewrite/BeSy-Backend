package de.hs_esslingen.besy.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.model.Address}
 */
@Value
public class AddressDto implements Serializable {
    String addressName;
    String addressBuildingName;
    String addressStreet;
    String addressBuildingNumber;
    String addressTown;
    String addressPostalCode;
    String addressCounty;
    CountryDto countryName;
    AddressTypeDto addressType;
    String addressComment;

    /**
     * DTO for {@link de.hs_esslingen.besy.model.Country}
     */
    @Value
    public static class CountryDto implements Serializable {
        String countryName;
        String countryGerman;
        String countryCode;
    }

    /**
     * DTO for {@link de.hs_esslingen.besy.model.AddressType}
     */
    @Value
    public static class AddressTypeDto implements Serializable {
        String addressType;
        String addressTypeComment;
    }
}