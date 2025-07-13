package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Faculty}
 */
@Value
public class FacultyResponseDTO implements Serializable {
    String facultyAbbr;
    String facultyLongName;
}