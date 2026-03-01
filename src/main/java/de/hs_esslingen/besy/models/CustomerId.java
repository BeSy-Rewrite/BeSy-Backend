package de.hs_esslingen.besy.models;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customer_id")
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CustomerId {

    @Column(name = "customer_id", insertable = false, updatable = false)
    private String customerId;

    @EmbeddedId
    private CustomerIdId id;

    @Column(name = "supplier_id", insertable = false, updatable = false)
    private Integer supplierId;

    @MapsId("supplierId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false, referencedColumnName = "id")
    private de.hs_esslingen.besy.models.Supplier supplier;

    @Column(name = "comment")
    private String comment;

}


