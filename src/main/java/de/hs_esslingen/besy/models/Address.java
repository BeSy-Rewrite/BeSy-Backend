package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
@Table(name = "address",
    uniqueConstraints = @UniqueConstraint(columnNames = {"building_name", "street", "building_number", "town", "postal_code", "county", "country"}))
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "building_name")
    private String buildingName;

    @Column(name = "street", nullable = true)
    private String street;

    @Column(name = "building_number")
    private String buildingNumber;

    @Column(name = "town", nullable = false)
    private String town;

    @Column(name = "postal_code", nullable = true, length = 20)
    private String postalCode;

    @Column(name = "county")
    private String county;

    @Column(name = "country")
    private String country;

    @Column(name = "comment")
    private String comment;

}