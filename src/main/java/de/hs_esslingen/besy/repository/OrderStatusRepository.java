package de.hs_esslingen.besy.repository;

import de.hs_esslingen.besy.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, String> {
}