package de.hs_esslingen.besy.dtos.request;

import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Quotation}
 */
@Value
public class QuotationRequestDTO implements Serializable {
    Short index;
    LocalDate quoteDate;
    BigDecimal price;
    String companyName;
    String companyCity;
}