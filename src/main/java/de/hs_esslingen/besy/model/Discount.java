package de.hs_esslingen.besy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "discount")
public class Discount {
    @EmbeddedId
    private DiscountId id;

    @MapsId("supplierName")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_name", nullable = false)
    private de.hs_esslingen.besy.model.Supplier supplierName;

    @Column(name = "discount_comment")
    private String discountComment;

}