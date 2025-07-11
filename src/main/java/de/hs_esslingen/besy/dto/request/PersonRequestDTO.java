package de.hs_esslingen.besy.dto.request;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.model.Person}
 */
@Value
public class PersonRequestDTO implements Serializable {
    String personGivenName;
    String personSurname;
    String personEmail;
    String personFax;
    String personPhone;
    String personTitle;
    String personComment;
    String addressNameId;
    String addressNameAddressComment;
    String facultyAbbrId;
}