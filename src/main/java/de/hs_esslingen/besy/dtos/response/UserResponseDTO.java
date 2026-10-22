package de.hs_esslingen.besy.dtos.response;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.User}
 */
@Value
public class UserResponseDTO implements Serializable {
    @NotNull
    long id;
    String surname;
    String name;
    String email;
}
