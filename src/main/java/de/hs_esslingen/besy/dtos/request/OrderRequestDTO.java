package de.hs_esslingen.besy.dtos.request;

import de.hs_esslingen.besy.enums.OrderStatus;
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
    String primaryCostCenterId;
    String primaryCostCenterFaculty;
    String bookingYear;
    Short autoIndex;
    OffsetDateTime createdAt;
    String legacyAlias;
    String ownerId;
    String contentDescription;
    OrderStatus status;
    String currencyShort;
    String comment;
    String commentForSupplier;
    String quoteNumber;
    String quoteSign;
    LocalDate quoteDate;
    BigDecimal quotePrice;
    Long deliveryPersonId;
    Long invoicePersonId;
    Long queriesPersonId;
    String customerId;
    String supplierName;
    String secondaryCostCenterId;
    BigDecimal fixedDiscount;
    BigDecimal percentageDiscount;
    BigDecimal cashbackPercentage;
    Short cashbackDays;
    OffsetDateTime lastUpdatedTime;
    Boolean flagDecisionCheapestOffer;
    Boolean flagDecisionSoleSupplier;
    Boolean flagDecisionContractPartner;
    Boolean flagDecisionOtherReasons;
    String decisionOtherReasonsDescription;
    Boolean flagEdvPermission;
    Boolean flagFurniturePermission;
    Boolean flagFurnitureRoom;
    Boolean flagInvestmentRoom;
    Boolean flagInvestmentStructuralMeasures;
    Boolean flagMediaPermission;
    String dfgKey;
    List<ItemRequestDTO> items;
    List<QuotationRequestDTO> quotations;
}