package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, String> {
}