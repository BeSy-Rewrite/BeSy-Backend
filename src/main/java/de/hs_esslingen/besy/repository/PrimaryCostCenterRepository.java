package de.hs_esslingen.besy.repository;

import de.hs_esslingen.besy.model.PrimaryCostCenter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrimaryCostCenterRepository extends JpaRepository<PrimaryCostCenter, String> {
  }