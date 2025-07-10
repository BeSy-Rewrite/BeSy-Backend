package de.hs_esslingen.besy.repository;

import de.hs_esslingen.besy.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
}
