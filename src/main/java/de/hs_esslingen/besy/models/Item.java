package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "item")
public class Item {
    @EmbeddedId
    private ItemId id;

    @MapsId("orderId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private de.hs_esslingen.besy.models.Order order;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_price_per_unit", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemPricePerUnit;

    @ColumnDefault("'1'")
    @Column(name = "item_quantity", columnDefinition = "bigint not null")
    private Long itemQuantity;

    @Column(name = "item_quantity_unit", length = 45)
    private String itemQuantityUnit;

    @Column(name = "item_article_id")
    private String itemArticleId;

    @ColumnDefault("''")
    @Column(name = "item_comment")
    private String itemComment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vat_value", nullable = false)
    private de.hs_esslingen.besy.models.Vat vatValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_list_abbr")
    private de.hs_esslingen.besy.models.PreferredList preferredListAbbr;

    @Column(name = "item_preferred_list_number", length = 20)
    private String itemPreferredListNumber;

    @ColumnDefault("'netto'")
    @Column(name = "item_vat_type", nullable = false)
    private String itemVatType;

}