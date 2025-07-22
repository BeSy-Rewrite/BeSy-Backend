package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Currency}
 */
@Value
public class CurrencyResponseDTO implements Serializable {
    String code;
    String name;
}