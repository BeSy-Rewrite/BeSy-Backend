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
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "email")
    private String email;

    @Column(name = "fax", length = 45)
    private String fax;

    @Column(name = "phone", length = 45)
    private String phone;

    @Column(name = "title", length = 45)
    private String title;

    @Column(name = "comment")
    private String comment;

    @Column(name = "address_id", insertable = false, updatable = false)
    private Integer addressId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(name = "person_gender", nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

}