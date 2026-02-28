package de.hs_esslingen.besy.dtos.request;

import de.hs_esslingen.besy.enums.Gender;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for updating {@link de.hs_esslingen.besy.models.Person}
 */
@Value
@Builder
public class PersonUpdateRequestDTO implements Serializable {
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
