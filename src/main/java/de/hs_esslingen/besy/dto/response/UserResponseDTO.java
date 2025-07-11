package de.hs_esslingen.besy.dto.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.model.User}
 */
@Value
public class UserResponseDTO implements Serializable {
    String userName;
    Long personId;
    String preferredCostCenterId;
}