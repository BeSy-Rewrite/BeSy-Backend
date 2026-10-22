package de.hs_esslingen.besy.dtos.request;

import java.io.Serializable;
import java.util.Map;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.UserPreferences}
 */
@Value
public class UserPreferencesRequestDTO implements Serializable {
    @NotNull
    String preferenceType;
    @NotNull
    Map<String, Object> preferences;
}
