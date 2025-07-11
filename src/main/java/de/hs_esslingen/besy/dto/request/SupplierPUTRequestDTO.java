package de.hs_esslingen.besy.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.hs_esslingen.besy.jackson.NumericBooleanDeserzializer;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link de.hs_esslingen.besy.model.Supplier}
 */
@Value
public class SupplierPUTRequestDTO implements Serializable {
    String supplierName;
    String supplierPhone;
    String supplierFax;
    String supplierComment;
    String supplierWebsite;
    String supplierVatId;

    Boolean supplierFlagPreferred;

    BigDecimal supplierCashbackPercentage;
    Short supplierCashbackDays;
    String addressType;
    String supplierBuildingName;
    String supplierStreet;
    String supplierBuildingNumber;
    String supplierPostalCode;
    String supplierTown;
    String supplierCounty;
    String countryName;
}