package de.hs_esslingen.besy.models;

import lombok.Builder;
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
@Builder
public class CostCenter {
    @Id
    @Column(name = "id", nullable = false, length = 20)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "begin_date")
    private LocalDate beginDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "comment")
    private String comment;

}
