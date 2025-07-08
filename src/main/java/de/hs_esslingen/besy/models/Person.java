package de.hs_esslingen.besy.models;

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
    @Column(name = "person_id", columnDefinition = "bigint not null")
    private Long id;

    @Column(name = "person_given_name", nullable = false)
    private String personGivenName;

    @Column(name = "person_surname", nullable = false)
    private String personSurname;

    @ColumnDefault("'m'")
    @Column(name = "person_gender", nullable = false)
    private String personGender;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_name", nullable = false)
    private Address addressName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_abbr")
    private Faculty facultyAbbr;

}