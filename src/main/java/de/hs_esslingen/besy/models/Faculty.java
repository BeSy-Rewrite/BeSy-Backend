package de.hs_esslingen.besy.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "faculty")
public class Faculty {
    @Id
    @Column(name = "faculty_abbr", nullable = false, length = 10)
    private String facultyAbbr;

    @Column(name = "faculty_long_name", nullable = false)
    private String facultyLongName;

}