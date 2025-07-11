package de.hs_esslingen.besy.dto.request;

import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link de.hs_esslingen.besy.model.Supplier}
 */
@Value
public class SupplierRequestDTO implements Serializable {
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
    String countryName;
    String addressType;
    LocalDate supplierDeactivatedDate;
    BigDecimal supplierCashbackPercentage;
    Short supplierCashbackDays;
}