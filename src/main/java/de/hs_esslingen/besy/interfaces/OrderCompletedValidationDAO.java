package de.hs_esslingen.besy.interfaces;

import de.hs_esslingen.besy.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    
    Short autoIndex;

    @NotNull
    LocalDateTime createdDate;

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
    Integer supplierId;

    @NotNull
    String secondaryCostCenterId;

    BigDecimal fixedDiscount;

    BigDecimal percentageDiscount;

    BigDecimal cashbackPercentage;

    Short cashbackDays;

    LocalDateTime lastUpdatedTime;

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