package de.hs_esslingen.besy.repository;

import de.hs_esslingen.besy.model.PreferredList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferredListRepository extends JpaRepository<PreferredList, String> {
}