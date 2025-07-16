package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.User}
 */
@Value
public class UserResponseDTO implements Serializable {
    String userName;
    Long personId;
    String preferredCostCenterId;
}