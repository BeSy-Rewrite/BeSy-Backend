package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.User;
import de.hs_esslingen.besy.models.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Integer> {
    List<UserPreferences> getUserPreferencesByUser_IdAndPreferenceType(Integer userId, String preferenceType);

    void deleteByIdAndUser(Integer id, User user);
}