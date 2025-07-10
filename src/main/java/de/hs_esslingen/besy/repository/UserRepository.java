package de.hs_esslingen.besy.repository;

import de.hs_esslingen.besy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}