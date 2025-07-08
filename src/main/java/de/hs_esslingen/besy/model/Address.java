package de.hs_esslingen.besy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_name", nullable = false)
    private de.hs_esslingen.besy.model.Country countryName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_type", nullable = false)
    private de.hs_esslingen.besy.model.AddressType addressType;

    @Column(name = "address_comment")
    private String addressComment;

}