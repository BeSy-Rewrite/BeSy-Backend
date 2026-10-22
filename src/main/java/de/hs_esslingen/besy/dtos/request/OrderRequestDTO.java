package de.hs_esslingen.besy.dtos.request;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Order}
 */
@Value
public class OrderRequestDTO implements Serializable {
    String primaryCostCenterId;
    @Size(min = 2, max = 2)
    String bookingYear;
    String legacyAlias;
    Long ownerId;
    @NotNull
    String contentDescription;
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
    Integer supplierId;
    String secondaryCostCenterId;
    BigDecimal fixedDiscount;
    BigDecimal percentageDiscount;
    BigDecimal cashbackPercentage;
    Short cashbackDays;
    Boolean flagDecisionCheapestOffer;
    Boolean flagDecisionMostEconomicalOffer;
    Boolean flagDecisionSoleSupplier;
    Boolean flagDecisionContractPartner;
    Boolean flagDecisionPreferredSupplierList;
    Boolean flagDecisionOtherReasons;
    String decisionOtherReasonsDescription;
    String dfgKey;
    Integer deliveryAddressId;
    Integer invoiceAddressId;
}
