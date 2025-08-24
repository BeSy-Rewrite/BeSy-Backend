package de.hs_esslingen.besy.interfaces;

import de.hs_esslingen.besy.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Order}
 */
@Getter @Setter @AllArgsConstructor
@Value
public class OrderCompletedValidationDAO {

    @NotNull
    Long id;

    @NotNull
    String primaryCostCenterId;

    @NotNull
    String bookingYear;

    @NotNull
    Short autoIndex;

    @NotNull
    OffsetDateTime createdDate;

    String legacyAlias;

    @NotNull
    Integer ownerId;

    @NotNull
    String contentDescription;

    @NotNull
    OrderStatus status;

    @NotNull
    String currencyShort;

    String comment;

    String commentForSupplier;

    String quoteNumber;

    String quoteSign;

    LocalDate quoteDate;

    BigDecimal quotePrice;

    @NotNull
    Long deliveryPersonId;

    @NotNull
    Long invoicePersonId;

    @NotNull
    Long queriesPersonId;

    @NotNull
    String customerId;

    @NotNull
    Integer supplierId;

    @NotNull
    String secondaryCostCenterId;

    BigDecimal fixedDiscount;

    BigDecimal percentageDiscount;

    BigDecimal cashbackPercentage;

    Short cashbackDays;

    @NotNull
    OffsetDateTime lastUpdatedTime;

    Boolean flagDecisionCheapestOffer;
    Boolean flagDecisionMostEconomicalOffer;
    Boolean flagDecisionSoleSupplier;
    Boolean flagDecisionContractPartner;
    Boolean flagDecisionPreferredSupplierList;
    Boolean flagDecisionOtherReasons;
    String decisionOtherReasonsDescription;

    String dfgKey;

    @NotNull
    Integer deliveryAddressId;

    @NotNull
    Integer invoiceAddressId;
}