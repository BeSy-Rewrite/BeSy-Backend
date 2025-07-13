package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.PrimaryCostCenter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrimaryCostCenterRepository extends JpaRepository<PrimaryCostCenter, String> {
  }