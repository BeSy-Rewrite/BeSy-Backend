package de.hs_esslingen.besy.dtos.request;

import de.hs_esslingen.besy.enums.PreferredList;
import de.hs_esslingen.besy.enums.VatType;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Item}
 */
@Value
public class ItemRequestDTO implements Serializable {
    String name;
    BigDecimal pricePerUnit;
    Long quantity;
    String quantityUnit;
    String articleId;
    String comment;
    String vatValue;
    PreferredList preferredList;
    String preferredListNumber;
    VatType vatType;
}