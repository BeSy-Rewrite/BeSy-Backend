package de.hs_esslingen.besy.repository;

import de.hs_esslingen.besy.model.Quotation;
import de.hs_esslingen.besy.model.QuotationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuotationRepository extends JpaRepository<Quotation, QuotationId> {
    List<Quotation> findByOrder_OrderId(Long orderOrderId);
}