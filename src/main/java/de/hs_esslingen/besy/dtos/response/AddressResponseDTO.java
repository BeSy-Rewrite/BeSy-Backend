package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Address}
 */
@Value
public class AddressResponseDTO implements Serializable {
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