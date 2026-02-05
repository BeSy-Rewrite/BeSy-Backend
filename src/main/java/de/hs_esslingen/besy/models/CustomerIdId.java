package de.hs_esslingen.besy.models;

import lombok.Builder;
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
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@Builder
public class CustomerIdId implements java.io.Serializable {
    private static final long serialVersionUID = -7933214224856243084L;
    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "supplier_id", nullable = false)
    private Integer supplierId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CustomerIdId entity = (CustomerIdId) o;
        return Objects.equals(this.supplierId, entity.supplierId) &&
                Objects.equals(this.customerId, entity.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supplierId, customerId);
    }

}
