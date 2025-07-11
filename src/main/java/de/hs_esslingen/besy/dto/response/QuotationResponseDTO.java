package de.hs_esslingen.besy.dto.response;

import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link de.hs_esslingen.besy.model.Quotation}
 */
@Value
public class QuotationResponseDTO implements Serializable {
    Long orderId;
    LocalDate quotationQuoteDate;
    BigDecimal quotationPrice;
    String quotationCompanyName;
    String quotationCompanyCity;
}