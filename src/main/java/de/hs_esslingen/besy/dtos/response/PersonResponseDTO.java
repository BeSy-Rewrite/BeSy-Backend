package de.hs_esslingen.besy.dtos.response;

import de.hs_esslingen.besy.enums.Gender;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Person}
 */
@Value
public class PersonResponseDTO implements Serializable {
    Long personId;
    String personGivenName;
    String personSurname;
    String personEmail;
    String personFax;
    String personPhone;
    String personTitle;
    String personComment;
    String addressName;
    String facultyAbbr;
    Gender personGender;
}