package de.hs_esslingen.besy.model;

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
public class Vat {
    @Id
    @Column(name = "vat_value", nullable = false, precision = 2)
    private BigDecimal id;

    @ColumnDefault("''")
    @Column(name = "vat_description", nullable = false)
    private String vatDescription;

}