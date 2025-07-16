package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Vat}
 */
@Value
public class VatResponseDTO implements Serializable {
    BigDecimal vatValue;
    String vatDescription;
}