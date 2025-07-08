package de.hs_esslingen.besy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "cost_center")
public class CostCenter {
    @Id
    @Column(name = "cost_center_id", nullable = false, length = 20)
    private String costCenterId;

    @Column(name = "cost_center_name", nullable = false)
    private String costCenterName;

    @Column(name = "cost_center_begin_date")
    private LocalDate costCenterBeginDate;

    @Column(name = "cost_center_end_date")
    private LocalDate costCenterEndDate;

    @Column(name = "cost_center_comment")
    private String costCenterComment;

}