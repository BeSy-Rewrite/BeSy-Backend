package de.hs_esslingen.besy.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DTO for {@link de.hs_esslingen.besy.model.Order}
 */
@Value
public class OrderResponseDTO implements Serializable {
    Long orderId;
    String primaryCostCenterId;
    String primaryCostCenterFaculty;
    String orderBookingYear;
    Short orderAutoIndex;
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
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    Boolean orderFlagDecisionCheapestOffer;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    Boolean orderFlagDecisionSoleSupplier;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    Boolean orderFlagDecisionContractPartner;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    Boolean orderFlagDecisionOtherReasons;

    String orderDecisionOtherReasonsDescription;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    Boolean orderFlagEdvPermission;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    Boolean orderFlagFurniturePermission;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    Boolean orderFlagFurnitureRoom;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    Boolean orderFlagInvestmentRoom;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    Boolean orderFlagInvestmentStructuralMeasures;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    Boolean orderFlagMediaPermission;

    String orderDfgKey;
}