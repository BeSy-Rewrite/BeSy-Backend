package de.hs_esslingen.besy.dto;

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
    Long id;
    String primaryCostCenterId;
    String primaryCostCenterFaculty;
    String orderBookingYear;
    Short orderAutoIndex;
    OffsetDateTime orderCreatedDate;
    String orderLegacyAlias;
    UserOrderResponseDTO ownerUserName;
    String orderContentDescription;
    OrderStatusOrderResponseDTO orderStatus;
    CurrencyOrderResponseDTO currencyShort;
    String orderComment;
    String orderCommentForSupplier;
    String orderQuoteNumber;
    String orderQuoteSign;
    LocalDate orderQuoteDate;
    BigDecimal orderQuotePrice;
    DeliveryPersonOrderResponseDTO deliveryPerson;
    InvoicePersonOrderResponseDTO invoicePerson;
    QueriesPersonOrderResponseDTO queriesPerson;
    CustomerOrderResponseDTO customerId;
    SecondaryCostCenterOrderResponseDTO secondaryCostCenter;
    BigDecimal orderFixedDiscount;
    BigDecimal orderPercentageDiscount;
    BigDecimal orderCashbackPercentage;
    Short orderCashbackDays;
    OffsetDateTime orderLastUpdatedTime;
    Boolean orderFlagDecisionCheapestOffer;
    Boolean orderFlagDecisionSoleSupplier;
    Boolean orderFlagDecisionContractPartner;
    Boolean orderFlagDecisionOtherReasons;
    String orderDecisionOtherReasonsDescription;
    Boolean orderFlagEdvPermission;
    Boolean orderFlagFurniturePermission;
    Boolean orderFlagFurnitureRoom;
    Boolean orderFlagInvestmentRoom;
    Boolean orderFlagInvestmentStructuralMeasures;
    Boolean orderFlagMediaPermission;
    String orderDfgKey;

    /**
     * DTO for {@link de.hs_esslingen.besy.model.User}
     */
    @Value
    public static class UserOrderResponseDTO implements Serializable {
        String ownerUserName;
    }

    /**
     * DTO for {@link de.hs_esslingen.besy.model.OrderStatus}
     */
    @Value
    public static class OrderStatusOrderResponseDTO implements Serializable {
        String orderStatus;
    }

    /**
     * DTO for {@link de.hs_esslingen.besy.model.Currency}
     */
    @Value
    public static class CurrencyOrderResponseDTO implements Serializable {
        String currencyShort;
    }

    /**
     * DTO for {@link de.hs_esslingen.besy.model.Person}
     */
    @Value
    public static class DeliveryPersonOrderResponseDTO implements Serializable {
        Long id;
    }

    /**
     * DTO for {@link de.hs_esslingen.besy.model.Person}
     */
    @Value
    public static class InvoicePersonOrderResponseDTO implements Serializable {
        Long id;
    }

    /**
     * DTO for {@link de.hs_esslingen.besy.model.Person}
     */
    @Value
    public static class QueriesPersonOrderResponseDTO implements Serializable {
        Long id;
    }

    /**
     * DTO for {@link de.hs_esslingen.besy.model.CustomerId}
     */
    @Value
    public static class CustomerOrderResponseDTO implements Serializable {
        String customerId;
    }

    /**
     * DTO for {@link de.hs_esslingen.besy.model.CostCenter}
     */
    @Value
    public static class SecondaryCostCenterOrderResponseDTO implements Serializable {
        String costCenterId;
    }
}