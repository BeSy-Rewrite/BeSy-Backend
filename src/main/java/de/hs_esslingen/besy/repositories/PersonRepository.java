package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    List<Person> findAllByActive(boolean active);

}
