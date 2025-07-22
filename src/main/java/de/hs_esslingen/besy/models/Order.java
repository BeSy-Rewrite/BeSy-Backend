package de.hs_esslingen.besy.models;

import de.hs_esslingen.besy.enums.OrderStatus;
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
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "primary_cost_center_id", insertable = false, updatable = false)
    private String primaryCostCenterId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "primary_cost_center_id", nullable = false)
    private CostCenter primaryCostCenter;

    @Column(name = "booking_year", nullable = false, length = 2)
    private String bookingYear;

    // TODO: Set this back to nullable = false
    @Column(name = "auto_index", nullable = true)
    private Short autoIndex;

    @Column(name = "created_date", nullable = false)
    private OffsetDateTime createdDate;

    @Column(name = "legacy_alias", length = 2)
    private String legacyAlias;

    @Column(name = "owner_user_id", insertable = false, updatable = false)
    private String ownerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id",referencedColumnName = "keycloak_uuid", nullable = false)
    private de.hs_esslingen.besy.models.User owner;

    @Column(name = "content_description", nullable = false)
    private String contentDescription;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "currency_short", insertable = false, updatable = false)
    private String currencyShort;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_short", nullable = false)
    private Currency currency;

    @ColumnDefault("''")
    @Column(name = "comment")
    private String comment;

    @Column(name = "comment_for_supplier", length = Integer.MAX_VALUE)
    private String commentForSupplier;

    @Column(name = "quote_number")
    private String quoteNumber;

    @Column(name = "quote_sign")
    private String quoteSign;

    @Column(name = "quote_date")
    private LocalDate quoteDate;

    @Column(name = "quote_price", precision = 10, scale = 2)
    private BigDecimal quotePrice;

    @Column(name = "delivery_person_id", insertable = false, updatable = false)
    private Long deliveryPersonId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_person_id", nullable = false)
    private de.hs_esslingen.besy.models.Person deliveryPerson;

    @Column(name = "invoice_person_id", insertable = false, updatable = false)
    private Long invoicePersonId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_person_id", nullable = false)
    private de.hs_esslingen.besy.models.Person invoicePerson;

    @Column(name = "queries_person_id", insertable = false, updatable = false)
    private Long queriesPersonId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "queries_person_id", nullable = false)
    private de.hs_esslingen.besy.models.Person queriesPerson;

    @Column(name = "customer_id", insertable = false, updatable = false)
    private String customerId;

    @Column(name = "supplier_id", insertable = false, updatable = false)
    private String supplierId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", nullable = false),
            @JoinColumn(name = "supplier_id", referencedColumnName = "supplier_id", nullable = false)
    })
    private CustomerId customer;

    @Column(name = "secondary_cost_center_id", insertable = false, updatable = false)
    private String secondaryCostCenterId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "secondary_cost_center_id", nullable = false)
    private CostCenter secondaryCostCenter;

    @ColumnDefault("0.00")
    @Column(name = "fixed_discount", precision = 10, scale = 2)
    private BigDecimal fixedDiscount;

    @ColumnDefault("0.00")
    @Column(name = "percentage_discount", precision = 4, scale = 2)
    private BigDecimal percentageDiscount;

    @Column(name = "cashback_percentage", precision = 3, scale = 2)
    private BigDecimal cashbackPercentage;

    @Column(name = "cashback_days")
    private Short cashbackDays;

    @Column(name = "last_updated_time", nullable = false)
    private OffsetDateTime lastUpdatedTime;

    @ColumnDefault("false")
    @Column(name = "flag_decision_cheapest_offer")
    private Boolean flagDecisionCheapestOffer;

    @ColumnDefault("false")
    @Column(name = "flag_decision_sole_supplier")
    private Boolean flagDecisionSoleSupplier;

    @ColumnDefault("false")
    @Column(name = "flag_decision_contract_partner")
    private Boolean flagDecisionContractPartner;

    @ColumnDefault("false")
    @Column(name = "flag_decision_other_reasons")
    private Boolean flagDecisionOtherReasons;

    @Column(name = "decision_other_reasons_description", length = Integer.MAX_VALUE)
    private String decisionOtherReasonsDescription;

    @ColumnDefault("false")
    @Column(name = "flag_edv_permission")
    private Boolean flagEdvPermission;

    @ColumnDefault("false")
    @Column(name = "flag_furniture_permission")
    private Boolean flagFurniturePermission;

    @ColumnDefault("false")
    @Column(name = "flag_furniture_room")
    private Boolean flagFurnitureRoom;

    @ColumnDefault("false")
    @Column(name = "flag_investment_room")
    private Boolean flagInvestmentRoom;

    @ColumnDefault("false")
    @Column(name = "flag_investment_structural_measures")
    private Boolean flagInvestmentStructuralMeasures;

    @ColumnDefault("false")
    @Column(name = "flag_media_permission")
    private Boolean flagMediaPermission;

    @Column(name = "dfg_key", length = 45)
    private String dfgKey;

}