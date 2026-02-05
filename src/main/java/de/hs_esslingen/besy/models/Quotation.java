package de.hs_esslingen.besy.models;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "quotation")
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Quotation {

    @EmbeddedId
    private QuotationId id;

    /**
     * The order ID column is mapped here with insertable=false, updatable=false
     * to allow easier JPQL queries and criteria building without affecting
     * persistence operations.
     * <p>
     * This field is read-only from JPA perspective and mirrors the orderId
     * from the embedded ID. It should NOT be removed, as queries or joins
     * relying on this field would break.
     */
    @Column(name = "order_id", insertable = false, updatable = false)
    private Long orderId;

    /**
     * The index column is mapped here with insertable=false, updatable=false
     * similarly to orderId for ease of querying and criteria API usage.
     * It mirrors the index value from the embedded ID.
     * <p>
     * Do not remove this field, as queries depending on this column will fail.
     */
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

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_city", nullable = false)
    private String companyCity;


    /**
     * Returns the index part of the embedded ID.
     *
     * This method is marked @Transient to avoid JPA mapping it again.
     * It provides a convenient way to access the index from the composite key.
     */
    @Transient
    public Short getIndex() {
        return id != null ? id.getIndex() : null;
    }

    /**
     * Returns the orderId part of the embedded ID.
     *
     * This method is marked @Transient to avoid JPA mapping it again.
     * It provides a convenient way to access the orderId from the composite key.
     */
    @Transient
    public Long getOrderId() {
        return id != null ? id.getOrderId() : null;
    }
}


