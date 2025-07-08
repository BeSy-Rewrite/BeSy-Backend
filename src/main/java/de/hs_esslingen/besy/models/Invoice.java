package de.hs_esslingen.besy.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @Column(name = "invoice_id", nullable = false)
    private String invoiceId;

    @Column(name = "cost_center_id", nullable = false, length = 20)
    private String costCenterId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "invoice_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal invoicePrice;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "invoice_comment")
    private String invoiceComment;

    @Column(name = "invoice_created_date", nullable = false)
    private OffsetDateTime invoiceCreatedDate;

}