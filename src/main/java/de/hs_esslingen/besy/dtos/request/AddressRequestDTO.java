package de.hs_esslingen.besy.dtos.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Address}
 */
@Value
public class AddressRequestDTO implements Serializable {
    String buildingName;
    String street;
    String buildingNumber;
    @NotNull
    String town;
    @Size(max = 20)
    String postalCode;
    String county;
    String country;
    String comment;
}
