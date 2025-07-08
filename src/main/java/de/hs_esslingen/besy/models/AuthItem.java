package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
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

    @Lob
    @Column(name = "description")
    private String description;

    @Lob
    @Column(name = "bizrule")
    private String bizrule;

    @Lob
    @Column(name = "data")
    private String data;

}