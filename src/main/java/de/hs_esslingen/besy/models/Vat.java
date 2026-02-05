package de.hs_esslingen.besy.models;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

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

}


