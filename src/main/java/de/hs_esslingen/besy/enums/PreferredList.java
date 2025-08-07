package de.hs_esslingen.besy.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PreferredList {
    RZ("Rechenzentrum"),
    TA("Technische Abteilung");

    private final String description;
}
