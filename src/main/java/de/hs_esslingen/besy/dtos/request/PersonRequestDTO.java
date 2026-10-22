package de.hs_esslingen.besy.dtos.request;

import java.io.Serializable;

import de.hs_esslingen.besy.enums.Gender;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Person}
 */
@Value
public class PersonRequestDTO implements Serializable {
    @NotNull
    String name;
    @NotNull
    String surname;

    String email;
    String fax;
    String phone;
    String title;
    String comment;
    Integer addressId;

    @NotNull
    Gender gender;
}
