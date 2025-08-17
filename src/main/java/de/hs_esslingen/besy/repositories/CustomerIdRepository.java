package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.CustomerId;
import de.hs_esslingen.besy.models.CustomerIdId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerIdRepository extends JpaRepository<CustomerId, CustomerIdId> {
    boolean existsByCustomerIdAndSupplierId(String customerId, Integer supplierId);

    List<CustomerId> findAllBySupplierId(Integer supplierId);
}