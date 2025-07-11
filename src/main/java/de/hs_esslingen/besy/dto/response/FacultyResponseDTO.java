package de.hs_esslingen.besy.dto.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.model.Faculty}
 */
@Value
public class FacultyResponseDTO implements Serializable {
    String facultyAbbr;
    String facultyLongName;
}