package de.hs_esslingen.besy.dtos.request;

import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Invoice}
 */
@Value
public class InvoiceRequestDTO implements Serializable {
    String id;
    String costCenterId;
    Long orderId;
    BigDecimal price;
    LocalDate date;
    String comment;
    Long paperlessId;
}