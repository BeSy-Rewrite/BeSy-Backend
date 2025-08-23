package de.hs_esslingen.besy.models;

import de.hs_esslingen.besy.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "primary_cost_center_id", nullable = true)
    private CostCenter primaryCostCenter;

    @Column(name = "booking_year", nullable = true, length = 2)
    private String bookingYear;

    @Column(name = "auto_index", nullable = true)
    private Short autoIndex;

    @Column(name = "created_date", nullable = false)
    private OffsetDateTime createdDate = OffsetDateTime.now();

    @Column(name = "legacy_alias", length = 2)
    private String legacyAlias;

    @Column(name = "owner_user_id", insertable = false, updatable = false)
    private Integer ownerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "owner_user_id",referencedColumnName = "id", nullable = true)
    private de.hs_esslingen.besy.models.User owner;

    @Column(name = "content_description", nullable = false)
    private String contentDescription;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "currency_short", insertable = false, updatable = false)
    private String currencyShort;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "currency_short", nullable = true)
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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "delivery_person_id", nullable = true)
    private de.hs_esslingen.besy.models.Person deliveryPerson;

    @Column(name = "invoice_person_id", insertable = false, updatable = false)
    private Long invoicePersonId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "invoice_person_id", nullable = true)
    private de.hs_esslingen.besy.models.Person invoicePerson;

    @Column(name = "queries_person_id", insertable = false, updatable = false)
    private Long queriesPersonId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "queries_person_id", nullable = true)
    private de.hs_esslingen.besy.models.Person queriesPerson;

    @Column(name = "customer_id", insertable = false, updatable = false)
    private String customerId;

    @Column(name = "supplier_id", insertable = false, updatable = false)
    private Integer supplierId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", nullable = true),
            @JoinColumn(name = "supplier_id", referencedColumnName = "supplier_id", nullable = true

            )
    })
    private CustomerId customer;

    @Column(name = "secondary_cost_center_id", insertable = false, updatable = false)
    private String secondaryCostCenterId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "secondary_cost_center_id", nullable = true)
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
    @Column(name = "flag_decision_most_economical_offer")
    private Boolean flagDecisionMostEconomicalOffer;

    @ColumnDefault("false")
    @Column(name = "flag_decision_sole_supplier")
    private Boolean flagDecisionSoleSupplier;

    @ColumnDefault("false")
    @Column(name = "flag_decision_contract_partner")
    private Boolean flagDecisionContractPartner;

    @ColumnDefault("false")
    @Column(name = "flag_decision_preferred_supplier_list")
    private Boolean flagDecisionPreferredSupplierList;

    @ColumnDefault("false")
    @Column(name = "flag_decision_other_reasons")
    private Boolean flagDecisionOtherReasons;

    @Column(name = "decision_other_reasons_description", length = Integer.MAX_VALUE)
    private String decisionOtherReasonsDescription;

    @Column(name = "dfg_key", length = 45)
    private String dfgKey;

    @Column(name = "delivery_address_id", insertable = false, updatable = false)
    private Integer deliveryAddressId;

    @Column(name = "invoice_address_id", insertable = false, updatable = false)
    private Integer invoiceAddressId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "delivery_address_id", nullable = true)
    private Address deliveryAddress;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "invoice_address_id", nullable = true)
    private Address invoiceAddress;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Approval approval;

    @PrePersist
    public void prePersist() {
        this.lastUpdatedTime = OffsetDateTime.now();
        this.approval = new Approval();
        this.approval.setOrder(this);
    }

    // only called if the data is actually changed
    @PreUpdate
    public void preUpdate() {
        this.lastUpdatedTime = OffsetDateTime.now();
    }

}