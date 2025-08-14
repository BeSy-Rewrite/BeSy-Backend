package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Address}
 */
@Value
public class AddressResponseDTO implements Serializable {
    Integer id;
    String buildingName;
    String street;
    String buildingNumber;
    String town;
    String postalCode;
    String county;
    String country;
    String comment;
}