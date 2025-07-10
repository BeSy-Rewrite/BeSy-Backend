package de.hs_esslingen.besy.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.model.Person}
 */
@Value
public class PersonResponseDTO implements Serializable {
    Long id;
    String personGivenName;
    String personSurname;
    String personEmail;
    String personFax;
    String personPhone;
    String personTitle;
    String personComment;
    AddressPersonResponseDTO addressName;
    FacultyPersonResponseDTO facultyAbbr;

    /**
     * DTO for {@link de.hs_esslingen.besy.model.Address}
     */
    @Value
    public static class AddressPersonResponseDTO implements Serializable {
        String addressName;
    }

    /**
     * DTO for {@link de.hs_esslingen.besy.model.Faculty}
     */
    @Value
    public static class FacultyPersonResponseDTO implements Serializable {
        String facultyAbbr;
    }
}