package de.hs_esslingen.besy.dtos.request;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Quotation}
 */
@Value
public class QuotationRequestDTO implements Serializable {
    @NotNull
    String companyName;
    @NotNull
    String companyCity;
    @NotNull
    LocalDate quoteDate;
    @NotNull
    BigDecimal price;
}
