package de.hs_esslingen.besy.dtos.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.hs_esslingen.besy.jackson.NumericBooleanDeserzializer;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Order}
 */
@Value
public class OrderRequestDTO implements Serializable {
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
    String customerId;
    String supplierName;
    String secondaryCostCenterId;
    BigDecimal orderFixedDiscount;
    BigDecimal orderPercentageDiscount;
    BigDecimal orderCashbackPercentage;
    Short orderCashbackDays;
    OffsetDateTime orderLastUpdatedTime;
    Boolean orderFlagDecisionCheapestOffer;
    Boolean orderFlagDecisionSoleSupplier;
    Boolean orderFlagDecisionContractPartner;
    Boolean orderFlagDecisionOtherReason;
    String orderDecisionOtherReasonsDescription;
    Boolean orderFlagEdvPermission;
    Boolean orderFlagFurniturePermission;
    Boolean orderFlagFurnitureRoom;
    Boolean orderFlagInvestmentRoom;
    Boolean orderFlagInvestmentStructuralMeasures;
    Boolean orderFlagMediaPermission;
    String orderDfgKey;
    List<ItemRequestDTO> items;
    List<QuotationRequestDTO> quotations;
}