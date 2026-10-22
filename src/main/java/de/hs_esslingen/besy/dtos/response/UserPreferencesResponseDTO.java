package de.hs_esslingen.besy.dtos.response;

import java.io.Serializable;
import java.util.Map;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.UserPreferences}
 */
@Value
public class UserPreferencesResponseDTO implements Serializable {
    @NotNull
    int id;
    @NotNull
    String preferenceType;
    @NotNull
    Map<String, Object> preferences;
}
