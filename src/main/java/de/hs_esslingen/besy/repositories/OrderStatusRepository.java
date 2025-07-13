package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, String> {
}