package de.hs_esslingen.besy.dtos.request;

import java.io.Serializable;
import java.math.BigDecimal;

import de.hs_esslingen.besy.enums.PreferredList;
import de.hs_esslingen.besy.enums.VatType;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Item}
 */
@Value
public class ItemRequestDTO implements Serializable {
    @NotNull
    String name;
    @NotNull
    BigDecimal pricePerUnit;
    @NotNull
    Long quantity;

    String quantityUnit;
    String articleId;
    String comment;
    PreferredList preferredList;
    String preferredListNumber;

    @NotNull
    BigDecimal vatValue;
    @NotNull
    VatType vatType;
}
