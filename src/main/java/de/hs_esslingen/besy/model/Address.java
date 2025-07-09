package de.hs_esslingen.besy.model;

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
    @Column(name = "address_name", nullable = false)
    private String addressName;

    @Column(name = "address_building_name")
    private String addressBuildingName;

    @Column(name = "address_street", nullable = false)
    private String addressStreet;

    @Column(name = "address_building_number")
    private String addressBuildingNumber;

    @Column(name = "address_town", nullable = false)
    private String addressTown;

    @Column(name = "address_postal_code", nullable = false, length = 20)
    private String addressPostalCode;

    @Column(name = "address_county")
    private String addressCounty;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_name")
    private de.hs_esslingen.besy.model.Country countryName;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_type")
    private de.hs_esslingen.besy.model.AddressType addressType;

    @Column(name = "address_comment")
    private String addressComment;

}