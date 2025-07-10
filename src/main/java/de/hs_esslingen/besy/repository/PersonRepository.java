package de.hs_esslingen.besy.repository;

import de.hs_esslingen.besy.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {


    @Query("SELECT p FROM Person p LEFT JOIN FETCH p.addressName LEFT JOIN FETCH p.facultyAbbr")
    List<Person> findAllWithAddressNameAndFacultyAbbr();
}
