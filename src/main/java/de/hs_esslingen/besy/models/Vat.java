package de.hs_esslingen.besy.models;

import java.math.BigDecimal;
import java.util.Objects;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vat")
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Vat {
    @Id
    @Column(name = "value", nullable = false, precision = 2)
    private BigDecimal value;

    @ColumnDefault("''")
    @Column(name = "description", nullable = false)
    private String description;

    /**
     * Value-object equality on the natural/business key ({@code value}).
     * {@code instanceof} (not {@code getClass() ==}) so this also works
     * correctly against Hibernate proxies. {@code compareTo} (not
     * {@code equals}) so that {@code 19.00} and {@code 19.0} are considered
     * the same rate regardless of scale.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vat other)) {
            return false;
        }
        return value != null && value.compareTo(other.value) == 0;
    }

    /**
     * Normalises the scale before hashing so that {@code 19.00} and
     * {@code 19.0} — which are equal per {@link #equals} — also hash equally,
     * satisfying the equals/hashCode contract.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value != null ? value.stripTrailingZeros() : null);
    }
}
