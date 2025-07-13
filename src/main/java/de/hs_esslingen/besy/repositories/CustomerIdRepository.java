package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.CustomerId;
import de.hs_esslingen.besy.models.CustomerIdId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerIdRepository extends JpaRepository<CustomerId, CustomerIdId> {
    boolean existsByCustomerIdAndSupplierName(String customerId, String supplierName);
}