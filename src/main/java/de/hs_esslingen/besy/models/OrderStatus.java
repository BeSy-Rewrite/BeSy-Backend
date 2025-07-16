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
@Table(name = "order_status")
public class OrderStatus {
    @Id
    @Column(name = "order_status", nullable = false, length = 20)
    private String orderStatus;

    @Column(name = "order_status_comment")
    private String orderStatusComment;

}