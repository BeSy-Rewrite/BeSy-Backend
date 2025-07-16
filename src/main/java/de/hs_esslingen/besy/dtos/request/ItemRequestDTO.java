package de.hs_esslingen.besy.dtos.request;

import de.hs_esslingen.besy.enums.VatType;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Item}
 */
@Value
public class ItemRequestDTO implements Serializable {
    Integer itemId;
    Long orderId;
    String itemName;
    BigDecimal itemPricePerUnit;
    Long itemQuantity;
    String itemQuantityUnit;
    String itemArticleId;
    String itemComment;
    String vatValue;
    String preferredListAbbr;
    String itemPreferredListNumber;
    VatType itemVatType;
}