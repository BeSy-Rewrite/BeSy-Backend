package de.hs_esslingen.besy.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderStatus {
    ABR("Abgerechnet"),
    ABS("Abgeschickt"),
    ARC("Archiviert"),
    DEL("Gelöscht"),
    INB("In Bearbeitung");

    private final String description;
}
