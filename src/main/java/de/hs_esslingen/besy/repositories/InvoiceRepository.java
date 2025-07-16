package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    Optional<Invoice> findByOrderId(Long orderId);
}