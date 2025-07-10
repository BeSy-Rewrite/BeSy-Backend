package de.hs_esslingen.besy.model;

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
    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    @Column(name = "supplier_phone", length = 45)
    private String supplierPhone;

    @Column(name = "supplier_fax", length = 45)
    private String supplierFax;

    @Column(name = "supplier_comment")
    private String supplierComment;

    @Column(name = "supplier_website")
    private String supplierWebsite;

    @Column(name = "supplier_vat_id", length = 20)
    private String supplierVatId;

    @ColumnDefault("false")
    @Column(name = "supplier_flag_preferred", nullable = false)
    private Boolean supplierFlagPreferred = false;

    @Column(name = "supplier_building_name")
    private String supplierBuildingName;

    @Column(name = "supplier_street", nullable = false)
    private String supplierStreet;

    @Column(name = "supplier_building_number")
    private String supplierBuildingNumber;

    @Column(name = "supplier_town", nullable = false)
    private String supplierTown;

    @Column(name = "supplier_postal_code", nullable = false)
    private String supplierPostalCode;

    @Column(name = "supplier_county")
    private String supplierCounty;

    @Column(name = "country_name", insertable = false, updatable = false)
    private String countryName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_name", nullable = false)
    private Country country;

    @Column(name = "address_type", insertable = false, updatable = false)
    private String addressType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_type", nullable = false)
    private AddressType address;

    @Column(name = "supplier_deactivated_date")
    private LocalDate supplierDeactivatedDate;

    @Column(name = "supplier_cashback_percentage", precision = 3, scale = 2)
    private BigDecimal supplierCashbackPercentage;

    @Column(name = "supplier_cashback_days")
    private Short supplierCashbackDays;

}