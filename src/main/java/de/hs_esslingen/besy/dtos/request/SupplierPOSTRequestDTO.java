package de.hs_esslingen.besy.dtos.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.hs_esslingen.besy.jackson.NumericBooleanDeserzializer;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Supplier}
 */
@Value
public class SupplierPOSTRequestDTO implements Serializable {
    String supplierName;
    String supplierPhone;
    String supplierFax;
    String supplierComment;
    String supplierWebsite;
    String supplierVatId;

    // Because someone in the past decided Booleans were too mainstream — now we speak in 0s and 1s 🙃
    @JsonDeserialize(using = NumericBooleanDeserzializer.class)
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