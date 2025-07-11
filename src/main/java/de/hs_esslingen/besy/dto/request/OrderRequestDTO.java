package de.hs_esslingen.besy.dto.request;

import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DTO for {@link de.hs_esslingen.besy.model.Order}
 */
@Value
public class OrderRequestDTO implements Serializable {
    String primaryCostCenterId;
    String primaryCostCenterFaculty;
    String orderBookingYear;
    String orderLegacyAlias;
    String ownerUserName;
    String orderContentDescription;
    String orderStatus;
    String currencyShort;
    String orderComment;
    String orderCommentForSupplier;
    String orderQuoteNumber;
    String orderQuoteSign;
    LocalDate orderQuoteDate;
    BigDecimal orderQuotePrice;
    Long deliveryPersonId;
    Long invoicePersonId;
    Long queriesPersonId;
    String supplierName;
    String customerId;
    String secondaryCostCenterId;
    BigDecimal orderFixedDiscount;
    BigDecimal orderPercentageDiscount;
    BigDecimal orderCashbackPercentage;
    Short orderCashbackDays;
    OffsetDateTime orderLastUpdatedTime;
    Boolean orderFlagDecisionCheapestOffer;
    Boolean orderFlagDecisionSoleSupplier;
    Boolean orderFlagDecisionContractPartner;
    String orderDecisionOtherReasonsDescription;
    Boolean orderFlagEdvPermission;
    Boolean orderFlagFurniturePermission;
    Boolean orderFlagFurnitureRoom;
    Boolean orderFlagInvestmentRoom;
    Boolean orderFlagInvestmentStructuralMeasures;
    Boolean orderFlagMediaPermission;
    String orderDfgKey;
}