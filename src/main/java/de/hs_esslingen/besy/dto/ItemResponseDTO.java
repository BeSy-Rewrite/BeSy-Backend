package de.hs_esslingen.besy.dto;

import de.hs_esslingen.besy.model.Item;
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
    String itemName;
    BigDecimal itemPricePerUnit;
    Long itemQuantity;
    String itemQuantityUnit;
    String itemArticleId;
    String itemComment;
    BigDecimal vatValue;
    String preferredListAbbr;
    String itemPreferredListNumber;
}