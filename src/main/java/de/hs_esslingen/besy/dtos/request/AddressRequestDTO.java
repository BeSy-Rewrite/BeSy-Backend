package de.hs_esslingen.besy.dtos.request;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Address}
 */
@Value
public class AddressRequestDTO implements Serializable {
    String buildingName;
    String street;
    String buildingNumber;
    String town;
    String postcode;
    String county;
    String country;
    String comment;
}