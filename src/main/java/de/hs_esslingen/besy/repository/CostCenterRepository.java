package de.hs_esslingen.besy.repository;

import de.hs_esslingen.besy.model.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CostCenterRepository extends JpaRepository<CostCenter, String> {
}