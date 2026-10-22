package de.hs_esslingen.besy.dtos.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Supplier}
 */
@Value
public class SupplierWithAddressResponseDTO implements Serializable {
    @JsonUnwrapped
    SupplierResponseDTO supplier;
    AddressResponseDTO address;
}
