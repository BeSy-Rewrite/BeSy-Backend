package de.hs_esslingen.besy.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.model.Address}
 */
@Value
public class AddressDto1 implements Serializable {
    String addressName;
    String addressBuildingName;
    String addressStreet;
    String addressBuildingNumber;
    String addressTown;
    String addressPostalCode;
    String addressCounty;
    String countryName;
    String addressType;
    String addressComment;
}