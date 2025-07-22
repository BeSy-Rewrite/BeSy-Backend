package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.Quotation;
import de.hs_esslingen.besy.models.QuotationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuotationRepository extends JpaRepository<Quotation, QuotationId> {
    List<Quotation> findByOrderId(Long orderId);
}