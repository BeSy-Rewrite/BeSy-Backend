package de.hs_esslingen.besy.dto.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.model.PreferredList}
 */
@Value
public class PreferredListResponseDTO implements Serializable {
    String preferredListAbbr;
    String preferredListDescription;
}