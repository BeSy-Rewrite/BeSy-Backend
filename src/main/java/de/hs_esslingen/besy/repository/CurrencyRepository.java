package de.hs_esslingen.besy.repository;

import de.hs_esslingen.besy.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, String> {
}