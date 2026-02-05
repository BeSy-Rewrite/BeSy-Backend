package de.hs_esslingen.besy.models;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "supplier")
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "phone", length = 45)
    private String phone;

    @Column(name = "fax", length = 45)
    private String fax;

    @Column(name = "email", length = 45)
    private String email;

    @Column(name = "comment")
    private String comment;

    @Column(name = "website")
    private String website;

    @Column(name = "vat_id", length = 20)
    private String vatId;

    @ColumnDefault("false")
    @Column(name = "flag_preferred", nullable = false)
    private Boolean flagPreferred = false;

    @Column(name = "deactivated_date")
    private LocalDate deactivatedDate;

    // Keep this for compatability with SupplierRepository
    @Column(name = "address_id", insertable = false, updatable = false)
    private Integer addressId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id", nullable = false)
    private Address address;

}


