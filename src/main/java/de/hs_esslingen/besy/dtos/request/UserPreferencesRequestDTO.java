package de.hs_esslingen.besy.dtos.request;

import lombok.Value;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link de.hs_esslingen.besy.models.User}
 */
@Value
public class UserPreferencesRequestDTO implements Serializable {
    Set<String> orderFilterPreferences;
}