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
@Table(name = "preferred_list")
public class PreferredList {
    @Id
    @Column(name = "preferred_list_abbr", nullable = false, length = 2)
    private String preferredListAbbr;

    @Column(name = "preferred_list_description", nullable = false)
    private String preferredListDescription;

}