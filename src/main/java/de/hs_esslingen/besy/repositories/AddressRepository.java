package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.enums.AddressOwnerType;
import de.hs_esslingen.besy.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    List<Address> getAddressByOwnerType(AddressOwnerType ownerType);
}
