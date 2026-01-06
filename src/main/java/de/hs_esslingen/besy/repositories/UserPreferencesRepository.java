package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.enums.PreferenceType;
import de.hs_esslingen.besy.models.User;
import de.hs_esslingen.besy.models.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Integer> {
    List<UserPreferences> getUserPreferencesByUser_IdAndPreferenceType(Integer userId, PreferenceType preferenceType);

    void deleteByIdAndUser(Integer id, User user);
}