package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "building_name")
    private String buildingName;

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "building_number")
    private String buildingNumber;

    @Column(name = "town", nullable = false)
    private String town;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "county")
    private String county;

    @Column(name = "country", insertable = false, updatable = false)
    private String country;

    @Column(name = "comment")
    private String comment;

}