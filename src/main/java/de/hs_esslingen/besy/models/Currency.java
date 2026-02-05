package de.hs_esslingen.besy.models;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "currency")
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Currency {
    @Id
    @Column(name = "code", nullable = false, length = 3)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;
}


