package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressTypeRepository extends JpaRepository<AddressType, String> {
}
