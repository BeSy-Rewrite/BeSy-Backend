package de.hs_esslingen.besy.models;

import de.hs_esslingen.besy.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Column(name = "person_given_name", nullable = false)
    private String personGivenName;

    @Column(name = "person_surname", nullable = false)
    private String personSurname;

    @Column(name = "person_email")
    private String personEmail;

    @Column(name = "person_fax", length = 45)
    private String personFax;

    @Column(name = "person_phone", length = 45)
    private String personPhone;

    @Column(name = "person_title", length = 45)
    private String personTitle;

    @Column(name = "person_comment")
    private String personComment;

    @Column(name = "address_name", insertable = false, updatable = false)
    private String addressName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_name", nullable = false)
    private Address address;

    @Column(name = "faculty_abbr", insertable = false, updatable = false)
    private String facultyAbbr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_abbr")
    private Faculty faculty;

    @Column(name = "person_gender", nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender personGender;

}