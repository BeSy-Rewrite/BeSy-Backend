package de.hs_esslingen.besy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "\"order\"")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "primary_cost_center_id", nullable = false, length = 20)
    private String primaryCostCenterId;

    @Column(name = "primary_cost_center_faculty", nullable = false, length = 10)
    private String primaryCostCenterFaculty;

    @Column(name = "order_booking_year", nullable = false, length = 2)
    private String orderBookingYear;

    @Column(name = "order_auto_index", nullable = false)
    private Short orderAutoIndex;

    @Column(name = "order_created_date", nullable = false)
    private OffsetDateTime orderCreatedDate;

    @Column(name = "order_legacy_alias", length = 2)
    private String orderLegacyAlias;

    @Column(name = "owner_user_name", insertable = false, updatable = false)
    private String ownerUserName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_name", nullable = false)
    private de.hs_esslingen.besy.model.User owner;

    @Column(name = "order_content_description", nullable = false)
    private String orderContentDescription;

    @Column(name = "order_status", insertable = false, updatable = false)
    private String orderStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_status", nullable = false)
    private de.hs_esslingen.besy.model.OrderStatus orderStatusRef;

    @Column(name = "currency_short", insertable = false, updatable = false)
    private String currencyShort;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_short", nullable = false)
    private Currency currency;

    @ColumnDefault("''")
    @Column(name = "order_comment")
    private String orderComment;

    @Column(name = "order_comment_for_supplier", length = Integer.MAX_VALUE)
    private String orderCommentForSupplier;

    @Column(name = "order_quote_number")
    private String orderQuoteNumber;

    @Column(name = "order_quote_sign")
    private String orderQuoteSign;

    @Column(name = "order_quote_date")
    private LocalDate orderQuoteDate;

    @Column(name = "order_quote_price", precision = 10, scale = 2)
    private BigDecimal orderQuotePrice;

    @Column(name = "delivery_person_id", insertable = false, updatable = false)
    private String deliveryPersonId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_person_id", nullable = false)
    private de.hs_esslingen.besy.model.Person deliveryPerson;

    @Column(name = "invoice_person_id", insertable = false, updatable = false)
    private String invoicePersonId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_person_id", nullable = false)
    private de.hs_esslingen.besy.model.Person invoicePerson;

    @Column(name = "queries_person_id", insertable = false, updatable = false)
    private String queriesPersonId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "queries_person_id", nullable = false)
    private de.hs_esslingen.besy.model.Person queriesPerson;

    @Column(name = "customer_id", insertable = false, updatable = false)
    private String customerId;

    @Column(name = "supplier_name", insertable = false, updatable = false)
    private String supplierName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", nullable = false),
            @JoinColumn(name = "supplier_name", referencedColumnName = "supplier_name", nullable = false)
    })
    private CustomerId customer;

    @Column(name = "secondary_cost_center_id", insertable = false, updatable = false)
    private String secondaryCostCenterId;

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

    @Column(name = "order_cashback_days")
    private Short orderCashbackDays;

    @Column(name = "order_last_updated_time", nullable = false)
    private OffsetDateTime orderLastUpdatedTime;

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

    @Column(name = "order_decision_other_reasons_description", length = Integer.MAX_VALUE)
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