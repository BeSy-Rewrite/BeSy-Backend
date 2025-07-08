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
@Table(name = "settings")
public class Setting {
    @Id
    @Column(name = "settings_key", nullable = false)
    private String settingsKey;

    @Column(name = "settings_value", nullable = false)
    private String settingsValue;

}