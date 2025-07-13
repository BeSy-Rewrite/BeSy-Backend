package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.PreferredList}
 */
@Value
public class PreferredListResponseDTO implements Serializable {
    String preferredListAbbr;
    String preferredListDescription;
}