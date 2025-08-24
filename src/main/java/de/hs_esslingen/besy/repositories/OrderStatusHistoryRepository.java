package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
  List<OrderStatusHistory> findAllByOrderId(Long orderId);
}