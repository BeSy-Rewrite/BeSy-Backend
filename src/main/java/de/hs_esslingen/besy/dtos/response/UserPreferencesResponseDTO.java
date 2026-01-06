package de.hs_esslingen.besy.dtos.response;

import de.hs_esslingen.besy.enums.PreferenceType;
import lombok.Value;

import java.io.Serializable;
import java.util.Map;

/**
 * DTO for {@link de.hs_esslingen.besy.models.UserPreferences}
 */
@Value
public class UserPreferencesResponseDTO implements Serializable {
    Integer id;
    PreferenceType preferenceType;
    Map<String, Object> preferences;
}