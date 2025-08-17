package de.hs_esslingen.besy.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class QuotationId implements java.io.Serializable {
    private static final long serialVersionUID = -8150342973464107154L;
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "index", nullable = false)
    private Short index;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        QuotationId entity = (QuotationId) o;
        return Objects.equals(this.index, entity.index) &&
                Objects.equals(this.orderId, entity.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, orderId);
    }

}