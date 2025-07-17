package de.hs_esslingen.besy.models;

import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.enums.PreferredList;
import de.hs_esslingen.besy.enums.VatType;
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

    @Column(name = "item_id", insertable = false, updatable = false)
    private Integer itemId;

    @EmbeddedId
    private ItemId id;

    @Column(name = "order_id", insertable = false, updatable = false)
    private Long orderId;

    @MapsId("orderId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    private de.hs_esslingen.besy.models.Order order;

    @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
    private String name;

    @Column(name = "price_per_unit", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerUnit;

    @ColumnDefault("1")
    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "quantity_unit", length = 45)
    private String quantityUnit;

    @Column(name = "article_id")
    private String articleId;

    @ColumnDefault("''")
    @Column(name = "comment")
    private String comment;

    @Column(name = "vat_value", insertable = false, updatable = false)
    private String vatValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vat_value", nullable = false)
    private de.hs_esslingen.besy.models.Vat vat;

    @Column(name = "preferred_list", nullable = false)
    @Enumerated(EnumType.STRING)
    private PreferredList preferredList;

    @ColumnDefault("'netto'")
    @Column(name = "item_vat_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private VatType vatType;

}