package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link de.hs_esslingen.besy.models.User}
 */
@Value
public class UserPreferencesResponseDTO implements Serializable {
    Set<String> orderFilterPreferences;
}