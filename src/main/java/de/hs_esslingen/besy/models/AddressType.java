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
@Table(name = "address_type")
public class AddressType {
    @Id
    @Column(name = "address_type", nullable = false, length = 20)
    private String addressType;

    @Column(name = "address_type_comment")
    private String addressTypeComment;

}