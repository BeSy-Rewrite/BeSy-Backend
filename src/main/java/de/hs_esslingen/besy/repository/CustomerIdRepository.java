package de.hs_esslingen.besy.repository;

import de.hs_esslingen.besy.model.CustomerId;
import de.hs_esslingen.besy.model.CustomerIdId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerIdRepository extends JpaRepository<CustomerId, CustomerIdId> {
}