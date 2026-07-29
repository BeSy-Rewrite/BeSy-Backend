package de.hs_esslingen.besy.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import de.hs_esslingen.besy.models.User;
import de.hs_esslingen.besy.models.UserPreferences;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
    List<UserPreferences> getUserPreferencesByUser_IdAndPreferenceType(long userId, String preferenceType);

    void deleteByIdAndUser(Integer id, User user);

    Boolean existsByIdAndUser_Id(Integer id, Long userId);

    UserPreferences findByIdAndUser(Integer id, User user);
}
