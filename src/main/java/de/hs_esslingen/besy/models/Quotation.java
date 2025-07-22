package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "quotation")
public class Quotation {

    @EmbeddedId
    private QuotationId id;

    @Column(name = "order_id", insertable = false, updatable = false)
    private Long orderId;

    @Column(name = "index", insertable = false, updatable = false)
    private Short index;

    @MapsId("orderId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "quote_date", nullable = false)
    private LocalDate quoteDate;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "supplier_id", insertable = false, updatable = false)
    private Long supplierId;

    @ManyToOne
    @JoinColumn(name = "supplier_id", referencedColumnName ="id", nullable = false)
    private Supplier supplier;

}