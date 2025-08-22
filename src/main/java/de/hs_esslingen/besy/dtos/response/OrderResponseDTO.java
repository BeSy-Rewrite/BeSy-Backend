package de.hs_esslingen.besy.dtos.response;

import de.hs_esslingen.besy.enums.OrderStatus;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Order}
 */
@Value
public class OrderResponseDTO implements Serializable {
    Long id;
    String primaryCostCenterId;
    String bookingYear;
    Short autoIndex;
    OffsetDateTime createdDate;
    String legacyAlias;
    Integer ownerId;
    String contentDescription;
    OrderStatus status;
    CurrencyResponseDTO currency;
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
    Integer supplierId;
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
    String dfgKey;
    Integer deliveryAddressId;
    Integer invoiceAddressId;
}