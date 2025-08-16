package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findTopByPrimaryCostCenterIdAndBookingYearOrderByAutoIndexDesc(String primaryCostCenterId, String bookingYear);
}
