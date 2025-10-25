package de.hs_esslingen.besy.models;

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

    /**
     * The item ID column is part of the composite primary key {@link ItemId}.
     * <p>
     * This field is marked {@code insertable = false, updatable = false} because
     * it is managed as part of the embedded ID.
     * <p>
     * It is included as a separate field to allow easier and more efficient querying
     * and filtering on this specific column without having to navigate through the embedded ID.
     * <p>
     * <strong>Removing this line will introduce breaking changes!</strong>
     */
    @Column(name = "item_id", insertable = false, updatable = false)
    private Integer itemId;

    @EmbeddedId
    private ItemId id;

    /**
     * The order ID column is part of the composite primary key {@link ItemId}.
     * <p>
     * This field is marked {@code insertable = false, updatable = false} because
     * it is managed as part of the embedded ID.
     * <p>
     * It is included as a separate field to allow easier and more efficient querying
     * and filtering on this specific column without having to navigate through the embedded ID.
     * <p>
     * <strong>Removing this line will introduce breaking changes!</strong>
     */
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
    private BigDecimal vatValue;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "vat_value", nullable = false)
    private de.hs_esslingen.besy.models.Vat vat;

    @Column(name = "preferred_list", nullable = true)
    @Enumerated(EnumType.STRING)
    private PreferredList preferredList;

    @Column(name = "preferred_list_number")
    private String preferredListNumber;

    @ColumnDefault("'netto'")
    @Column(name = "vat_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private VatType vatType;

    @Column(name = "migrated_to_insy", nullable = false)
    Boolean migratedToInsy;


    /**
     * Returns the item ID part of the composite primary key.
     * <p>
     * This method provides convenient access to the {@code itemId} embedded inside the {@link ItemId} composite key.
     * It is marked {@code @Transient} because it does not correspond to a separate database column,
     * but derives its value from the embedded ID.
     *
     * @return the item ID if the embedded ID is not null; otherwise, {@code null}.
     */
    @Transient
    public Integer getItemId() {
        return id != null ? id.getItemId() : null;
    }


    /**
     * Returns the order ID part of the composite primary key.
     * <p>
     * This method provides convenient access to the {@code orderId} embedded inside the {@link ItemId} composite key.
     * It is marked {@code @Transient} because it does not correspond to a separate database column,
     * but derives its value from the embedded ID.
     *
     * @return the order ID if the embedded ID is not null; otherwise, {@code null}.
     */
    @Transient
    public Long getOrderId() {
        return id != null ? id.getOrderId() : null;
    }
}