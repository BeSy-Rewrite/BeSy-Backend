package de.hs_esslingen.besy.dto.response;

import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link de.hs_esslingen.besy.model.Vat}
 */
@Value
public class VatResponseDTO implements Serializable {
    BigDecimal vatValue;
    String vatDescription;
}