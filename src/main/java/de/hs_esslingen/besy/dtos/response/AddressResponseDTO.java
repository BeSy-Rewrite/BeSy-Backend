package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Address}
 */
@Value
public class AddressResponseDTO implements Serializable {
    String name;
    String buildingName;
    String street;
    String buildingNumber;
    String townName;
    String postalCode;
    String county;
    String country;
    String comment;
}