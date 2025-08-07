package de.hs_esslingen.besy.dtos.response;

import de.hs_esslingen.besy.enums.PreferredList;
import de.hs_esslingen.besy.enums.VatType;
import de.hs_esslingen.besy.models.Item;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link Item}
 */
@Value
public class ItemResponseDTO implements Serializable {
    Long orderId;
    Integer itemId;
    String name;
    BigDecimal pricePerUnit;
    Long quantity;
    String quantityUnit;
    String articleId;
    String comment;
    VatResponseDTO vat;
    PreferredList preferredList;
    String preferredListNumber;
    VatType vatType;
}