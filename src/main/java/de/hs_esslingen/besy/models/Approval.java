package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "approvals")
public class Approval {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "flag_edv_permission")
    private Boolean flagEdvPermission = false;

    @Column(name = "flag_furniture_permission")
    private Boolean flagFurniturePermission = false;

    @Column(name = "flag_furniture_room")
    private Boolean flagFurnitureRoom = false;

    @Column(name = "flag_investment_room")
    private Boolean flagInvestmentRoom = false;

    @Column(name = "flag_investment_structural_measures")
    private Boolean flagInvestmentStructuralMeasures = false;

    @Column(name = "flag_media_permission")
    private Boolean flagMediaPermission = false;

}
