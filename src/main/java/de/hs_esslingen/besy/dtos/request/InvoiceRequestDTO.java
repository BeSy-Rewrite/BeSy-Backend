package de.hs_esslingen.besy.dtos.request;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Invoice}
 */
@Value
@Schema(description = "Datenübertragungsobjekt zur Erstellung einer Rechnung")
public class InvoiceRequestDTO implements Serializable {
    @NotNull
    @Schema(description = "ID der Rechnung")
    String id;
    @NotNull
    @Schema(description = "ID der zugehörigen Kostenstelle")
    String costCenterId;
    @NotNull
    @Schema(description = "Referenz auf die zugehörige Bestellung")
    Long orderId;
    @NotNull
    @Schema(description = "Rechnungsbetrag")
    BigDecimal price;
    @NotNull
    @Schema(description = "Rechnungsdatum")
    LocalDate date;

    @Schema(description = "Kommentar zur Rechnung")
    String comment;
    @Schema(description = "Referenz auf ein Dokument im papierlosen System")
    Long paperlessId;
}
