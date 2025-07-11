package de.hs_esslingen.besy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customer_id")
public class CustomerId {

    @Column(name = "customer_id", insertable = false, updatable = false)
    private String customerId;

    @EmbeddedId
    private CustomerIdId id;

    @Column(name = "supplier_name", insertable = false, updatable = false)
    private String supplierName;

    @MapsId("supplierName")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_name", nullable = false)
    private de.hs_esslingen.besy.model.Supplier supplier;

    @Column(name = "customer_id_comment")
    private String customerIdComment;

}