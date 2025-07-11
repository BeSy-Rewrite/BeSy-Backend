package de.hs_esslingen.besy.repository;

import de.hs_esslingen.besy.model.Vat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface VatRepository extends JpaRepository<Vat, BigDecimal> {
}