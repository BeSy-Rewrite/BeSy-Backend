package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByKeycloakUUID(String keycloakUUID);

    @Query("SELECT u FROM User u WHERE u.keycloakUUID = :keycloakUUID")
    Optional<User> findOptionalByKeycloakUUID(@Param("keycloakUUID") String keycloakUUID);
}