package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @Column(name = "invoice_id", nullable = false)
    private String invoiceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private de.hs_esslingen.besy.models.Order order;

    @Column(name = "invoice_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal invoicePrice;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "invoice_comment")
    private String invoiceComment;

    @Column(name = "invoice_created_date", nullable = false)
    private Instant invoiceCreatedDate;

}