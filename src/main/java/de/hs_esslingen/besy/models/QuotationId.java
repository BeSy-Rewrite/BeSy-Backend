package de.hs_esslingen.besy.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@Embeddable
public class QuotationId implements java.io.Serializable {
    private static final long serialVersionUID = -4877600400818167736L;
    @Column(name = "order_id", columnDefinition = "bigint not null")
    private Long orderId;

    @Column(name = "quotation_index", nullable = false)
    private Byte quotationIndex;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        QuotationId entity = (QuotationId) o;
        return Objects.equals(this.quotationIndex, entity.quotationIndex) &&
                Objects.equals(this.orderId, entity.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quotationIndex, orderId);
    }

}