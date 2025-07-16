package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.Vat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface VatRepository extends JpaRepository<Vat, BigDecimal> {
}