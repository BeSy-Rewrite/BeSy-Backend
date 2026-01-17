package de.hs_esslingen.besy.dtos.request;

import lombok.Value;

import java.io.Serializable;
import java.util.Map;

/**
 * DTO for {@link de.hs_esslingen.besy.models.UserPreferences}
 */
@Value
public class UserPreferencesRequestDTO implements Serializable {
    String preferenceType;
    Map<String, Object> preferences;
}