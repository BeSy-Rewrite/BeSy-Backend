package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customer_id")
public class CustomerId {
    @EmbeddedId
    private CustomerIdId id;

    @MapsId("supplierName")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_name", nullable = false)
    private de.hs_esslingen.besy.models.Supplier supplierName;

    @Column(name = "customer_id_comment")
    private String customerIdComment;

}