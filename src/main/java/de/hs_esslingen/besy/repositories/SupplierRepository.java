package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    Supplier getSupplierById(Integer id);

    @Query("SELECT DISTINCT s.addressId FROM Supplier s")
    List<Integer> findAllAddressId();
}
