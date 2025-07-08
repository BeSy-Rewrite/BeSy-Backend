package de.hs_esslingen.besy.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class DiscountId implements java.io.Serializable {
    private static final long serialVersionUID = 1997670693558585296L;
    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    @Column(name = "discount_value", nullable = false, precision = 4, scale = 2)
    private BigDecimal discountValue;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DiscountId entity = (DiscountId) o;
        return Objects.equals(this.supplierName, entity.supplierName) &&
                Objects.equals(this.discountValue, entity.discountValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supplierName, discountValue);
    }

}