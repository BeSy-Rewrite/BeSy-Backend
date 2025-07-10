package de.hs_esslingen.besy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "\"user\"")
public class User {
    @Id
    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "person_id", insertable = false, updatable = false)
    private Long personId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(name = "preferred_cost_center_id", insertable = false, updatable = false)
    private String preferredCostCenterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_cost_center_id")
    private CostCenter preferredCostCenter;

}