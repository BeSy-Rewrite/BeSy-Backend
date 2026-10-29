package de.hs_esslingen.besy.dtos.request;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(type = "object", additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
    Object preferences;
}
