package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.PreferredList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferredListRepository extends JpaRepository<PreferredList, String> {
}