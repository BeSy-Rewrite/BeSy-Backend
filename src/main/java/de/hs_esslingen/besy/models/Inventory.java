package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @Column(name = "inventory_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private de.hs_esslingen.besy.models.Order order;

    @Column(name = "inventory_name", nullable = false)
    private String inventoryName;

    @Column(name = "inventory_room", nullable = false, length = 10)
    private String inventoryRoom;

    @ColumnDefault("1")
    @Column(name = "inventory_count", nullable = false)
    private Long inventoryCount;

    @Column(name = "inventory_date", nullable = false)
    private LocalDate inventoryDate;

    @Column(name = "inventory_serial_number")
    private String inventorySerialNumber;

    @Column(name = "inventory_license_key")
    private String inventoryLicenseKey;

    @Column(name = "inventory_responsible_person")
    private String inventoryResponsiblePerson;

    @Column(name = "inventory_comment")
    private String inventoryComment;

}