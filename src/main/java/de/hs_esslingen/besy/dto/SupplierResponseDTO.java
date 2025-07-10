package de.hs_esslingen.besy.dto;

import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link de.hs_esslingen.besy.model.Supplier}
 */
@Value
public class SupplierResponseDTO implements Serializable {
    String supplierName;
    String supplierPhone;
    String supplierFax;
    String supplierComment;
    String supplierWebsite;
    String supplierVatId;
    Boolean supplierFlagPreferred;
    String supplierBuildingName;
    String supplierStreet;
    String supplierBuildingNumber;
    String supplierTown;
    String supplierPostalCode;
    String supplierCounty;
    CountryResponseDTO countryName;
    AddressTypeResponseDTO addressType;
    BigDecimal supplierCashbackPercentage;
    Short supplierCashbackDays;
}