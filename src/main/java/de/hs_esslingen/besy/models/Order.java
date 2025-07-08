package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "`order`")
public class Order {
    @Id
    @Column(name = "order_id", columnDefinition = "bigint not null")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private de.hs_esslingen.besy.models.PrimaryCostCenter primaryCostCenter;

    @Column(name = "order_booking_year", nullable = false, length = 2)
    private String orderBookingYear;

    @Column(name = "order_auto_index", nullable = false)
    private Short orderAutoIndex;

    @Column(name = "order_created_date", nullable = false)
    private Instant orderCreatedDate;

    @Column(name = "order_legacy_alias", length = 2)
    private String orderLegacyAlias;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_name", nullable = false)
    private de.hs_esslingen.besy.models.User ownerUserName;

    @Column(name = "order_content_description", nullable = false)
    private String orderContentDescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_status", nullable = false)
    private de.hs_esslingen.besy.models.OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_short", nullable = false)
    private Currency currencyShort;

    @ColumnDefault("''")
    @Column(name = "order_comment")
    private String orderComment;

    @Lob
    @Column(name = "order_comment_for_supplier")
    private String orderCommentForSupplier;

    @Column(name = "order_quote_number")
    private String orderQuoteNumber;

    @Column(name = "order_quote_sign")
    private String orderQuoteSign;

    @Column(name = "order_quote_date")
    private LocalDate orderQuoteDate;

    @Column(name = "order_quote_price", precision = 10, scale = 2)
    private BigDecimal orderQuotePrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_person_id", nullable = false)
    private de.hs_esslingen.besy.models.Person deliveryPerson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_person_id", nullable = false)
    private de.hs_esslingen.besy.models.Person invoicePerson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "queries_person_id", nullable = false)
    private de.hs_esslingen.besy.models.Person queriesPerson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private CustomerId customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "secondary_cost_center_id", nullable = false)
    private CostCenter secondaryCostCenter;

    @ColumnDefault("0.00")
    @Column(name = "order_fixed_discount", precision = 10, scale = 2)
    private BigDecimal orderFixedDiscount;

    @ColumnDefault("0.00")
    @Column(name = "order_percentage_discount", precision = 4, scale = 2)
    private BigDecimal orderPercentageDiscount;

    @Column(name = "order_cashback_percentage", precision = 3, scale = 2)
    private BigDecimal orderCashbackPercentage;

    @Column(name = "order_cashback_days", columnDefinition = "smallint")
    private Short orderCashbackDays;

    @Column(name = "order_last_updated_time", nullable = false)
    private Instant orderLastUpdatedTime;

    @ColumnDefault("false")
    @Column(name = "order_flag_decision_cheapest_offer")
    private Boolean orderFlagDecisionCheapestOffer;

    @ColumnDefault("false")
    @Column(name = "order_flag_decision_sole_supplier")
    private Boolean orderFlagDecisionSoleSupplier;

    @ColumnDefault("false")
    @Column(name = "order_flag_decision_contract_partner")
    private Boolean orderFlagDecisionContractPartner;

    @ColumnDefault("false")
    @Column(name = "order_flag_decision_other_reasons")
    private Boolean orderFlagDecisionOtherReasons;

    @Lob
    @Column(name = "order_decision_other_reasons_description")
    private String orderDecisionOtherReasonsDescription;

    @ColumnDefault("false")
    @Column(name = "order_flag_edv_permission")
    private Boolean orderFlagEdvPermission;

    @ColumnDefault("false")
    @Column(name = "order_flag_furniture_permission")
    private Boolean orderFlagFurniturePermission;

    @ColumnDefault("false")
    @Column(name = "order_flag_furniture_room")
    private Boolean orderFlagFurnitureRoom;

    @ColumnDefault("false")
    @Column(name = "order_flag_investment_room")
    private Boolean orderFlagInvestmentRoom;

    @ColumnDefault("false")
    @Column(name = "order_flag_investment_structural_measures")
    private Boolean orderFlagInvestmentStructuralMeasures;

    @ColumnDefault("false")
    @Column(name = "order_flag_media_permission")
    private Boolean orderFlagMediaPermission;

    @Column(name = "order_dfg_key", length = 45)
    private String orderDfgKey;

}