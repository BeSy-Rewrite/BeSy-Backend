package de.hs_esslingen.besy.dtos.response;

import de.hs_esslingen.besy.enums.Gender;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Person}
 */
@Value
public class PersonResponseDTO implements Serializable {
    Long id;
    String name;
    String surname;
    String email;
    String fax;
    String phone;
    String title;
    String comment;
    Integer addressId;
    Gender gender;
    Boolean active;
}