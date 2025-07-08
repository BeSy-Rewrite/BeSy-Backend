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
@Table(name = "country")
public class Country {
    @Id
    @Column(name = "country_name", nullable = false)
    private String countryName;

    @Column(name = "country_german", nullable = false)
    private String countryGerman;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

}