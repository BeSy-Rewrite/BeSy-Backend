package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.AddressType}
 */
@Value
public class AddressTypeResponseDTO implements Serializable {
    String addressType;
    String addressTypeComment;
}