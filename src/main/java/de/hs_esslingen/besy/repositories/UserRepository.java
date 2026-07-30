package de.hs_esslingen.besy.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.hs_esslingen.besy.models.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByKeycloakUUID(String keycloakUUID);

    @Query("SELECT u FROM User u WHERE u.keycloakUUID = :keycloakUUID")
    Optional<User> findOptionalByKeycloakUUID(@Param("keycloakUUID") String keycloakUUID);

    Optional<User> findOptionalByEmail(String email);
}
