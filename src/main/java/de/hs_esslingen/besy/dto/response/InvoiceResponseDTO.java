package de.hs_esslingen.besy.dto.response;

import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DTO for {@link de.hs_esslingen.besy.model.Invoice}
 */
@Value
public class InvoiceResponseDTO implements Serializable {
    String invoiceId;
    String costCenterId;
    Long orderId;
    BigDecimal invoicePrice;
    LocalDate invoiceDate;
    String invoiceComment;
    OffsetDateTime invoiceCreatedDate;
}