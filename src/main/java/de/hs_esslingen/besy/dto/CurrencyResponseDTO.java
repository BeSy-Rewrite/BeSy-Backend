package de.hs_esslingen.besy.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.model.Currency}
 */
@Value
public class CurrencyResponseDTO implements Serializable {
    String currencyShort;
    String currencyLong;
    String currencyComment;
}