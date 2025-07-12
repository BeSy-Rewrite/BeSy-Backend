package de.hs_esslingen.besy.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.hs_esslingen.besy.jackson.NumericBooleanDeserzializer;
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
    Long orderId; // Only used in POST & GET
    String primaryCostCenterId;
    String primaryCostCenterFaculty;
    String orderBookingYear;
    OffsetDateTime orderCreatedDate;
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

    // Because someone in the past decided Booleans were too mainstream — now we speak in 0s and 1s 🙃
    @JsonDeserialize(using = NumericBooleanDeserzializer.class)
    Boolean orderFlagDecisionCheapestOffer;

    @JsonDeserialize(using = NumericBooleanDeserzializer.class)
    Boolean orderFlagDecisionSoleSupplier;

    @JsonDeserialize(using = NumericBooleanDeserzializer.class)
    Boolean orderFlagDecisionContractPartner;

    @JsonDeserialize(using = NumericBooleanDeserzializer.class)
    Boolean orderFlagDecisionOtherReason;
    String orderDecisionOtherReasonsDescription;

    @JsonDeserialize(using = NumericBooleanDeserzializer.class)
    Boolean orderFlagEdvPermission;

    @JsonDeserialize(using = NumericBooleanDeserzializer.class)
    Boolean orderFlagFurniturePermission;

    @JsonDeserialize(using = NumericBooleanDeserzializer.class)
    Boolean orderFlagFurnitureRoom;

    @JsonDeserialize(using = NumericBooleanDeserzializer.class)
    Boolean orderFlagInvestmentRoom;

    @JsonDeserialize(using = NumericBooleanDeserzializer.class)
    Boolean orderFlagInvestmentStructuralMeasures;

    @JsonDeserialize(using = NumericBooleanDeserzializer.class)
    Boolean orderFlagMediaPermission;
    String orderDfgKey;
}