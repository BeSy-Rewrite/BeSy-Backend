package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    @Query("SELECT DISTINCT p.addressId FROM Person p")
    List<Integer> findAllAddressId();
}
