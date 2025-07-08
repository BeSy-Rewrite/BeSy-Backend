package de.hs_esslingen.besy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@Embeddable
public class CustomerIdId implements java.io.Serializable {
    private static final long serialVersionUID = -7933214224856243084L;
    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CustomerIdId entity = (CustomerIdId) o;
        return Objects.equals(this.supplierName, entity.supplierName) &&
                Objects.equals(this.customerId, entity.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supplierName, customerId);
    }

}