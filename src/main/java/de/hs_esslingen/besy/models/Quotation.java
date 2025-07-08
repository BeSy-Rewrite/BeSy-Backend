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

    @MapsId("orderId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "quotation_quote_date", nullable = false)
    private LocalDate quotationQuoteDate;

    @Column(name = "quotation_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal quotationPrice;

    @Column(name = "quotation_company_name", nullable = false)
    private String quotationCompanyName;

    @Column(name = "quotation_company_city")
    private String quotationCompanyCity;

}