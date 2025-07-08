package de.hs_esslingen.besy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "cash_expenditure")
public class CashExpenditure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cash_expenditure_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cost_center_id", nullable = false)
    private de.hs_esslingen.besy.model.CostCenter costCenter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_name", nullable = false)
    private de.hs_esslingen.besy.model.User userName;

    @Column(name = "cash_expenditure_price", nullable = false, precision = 9, scale = 2)
    private BigDecimal cashExpenditurePrice;

    @Column(name = "cash_expenditure_date", nullable = false)
    private LocalDate cashExpenditureDate;

    @Column(name = "cash_expenditure_description", nullable = false)
    private String cashExpenditureDescription;

    @Column(name = "cash_expenditure_created_date", nullable = false)
    private OffsetDateTime cashExpenditureCreatedDate;

}