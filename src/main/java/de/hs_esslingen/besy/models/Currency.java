package de.hs_esslingen.besy.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "currency")
public class Currency {
    @Id
    @Column(name = "currency_short", nullable = false, length = 3)
    private String currencyShort;

    @Column(name = "currency_long", nullable = false)
    private String currencyLong;

    @Column(name = "currency_comment")
    private String currencyComment;

}