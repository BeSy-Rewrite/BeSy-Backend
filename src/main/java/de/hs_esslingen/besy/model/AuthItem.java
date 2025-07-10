package de.hs_esslingen.besy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "auth_item")
public class AuthItem {
    @Id
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "type", nullable = false)
    private Integer type;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "bizrule", length = Integer.MAX_VALUE)
    private String bizrule;

    @Column(name = "data", length = Integer.MAX_VALUE)
    private String data;

}