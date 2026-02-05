package de.hs_esslingen.besy.models;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import de.hs_esslingen.besy.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "order_status_history")
@EntityListeners(AuditingEntityListener.class)
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "order_id", insertable = false, updatable = false)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "status", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "timestamp", nullable = false)
    @CreatedDate
    private LocalDateTime timestamp;

}


