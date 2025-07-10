package de.hs_esslingen.besy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "primary_cost_center")
public class PrimaryCostCenter {
    @Id
    @Column(name = "cost_center_id", nullable = false, length = 20)
    private String costCenterId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cost_center_id", nullable = false)
    private CostCenter costCenter;

    @Column(name = "faculty_abbr", insertable = false, updatable = false)
    private String facultyAbbr;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_abbr", nullable = false)
    private Faculty faculty;

}