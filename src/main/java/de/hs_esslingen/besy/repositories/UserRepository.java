package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByKeycloakUUID(String keycloakUUID);
}