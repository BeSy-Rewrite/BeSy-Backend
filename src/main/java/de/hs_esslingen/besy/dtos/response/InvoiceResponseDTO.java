package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Invoice}
 */
@Value
public class InvoiceResponseDTO implements Serializable {
    String id;
    String costCenterId;
    Long orderId;
    BigDecimal price;
    LocalDate date;
    String comment;
    OffsetDateTime createdDate;

}