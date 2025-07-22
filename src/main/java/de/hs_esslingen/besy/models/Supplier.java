package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "supplier")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "phone", length = 45)
    private String phone;

    @Column(name = "fax", length = 45)
    private String fax;

    @Column(name = "email", length = 45)
    private String email;

    @Column(name = "comment")
    private String comment;

    @Column(name = "website")
    private String website;

    @Column(name = "vat_id", length = 20)
    private String vatId;

    @ColumnDefault("false")
    @Column(name = "flag_preferred", nullable = false)
    private Boolean flagPreferred = false;

    @Column(name = "building_name")
    private String buildingName;

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "building_number")
    private String buildingNumber;

    @Column(name = "town", nullable = false)
    private String town;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "county")
    private String county;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "deactivated_date")
    private LocalDate deactivatedDate;

    @OneToOne
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

}