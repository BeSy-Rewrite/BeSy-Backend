package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CostCenterRepository extends JpaRepository<CostCenter, String> {
}