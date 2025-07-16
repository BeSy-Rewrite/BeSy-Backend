package de.hs_esslingen.besy.dtos.request;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Person}
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
    String addressName;
    String addressNameAddressComment;
    String facultyAbbr;
}