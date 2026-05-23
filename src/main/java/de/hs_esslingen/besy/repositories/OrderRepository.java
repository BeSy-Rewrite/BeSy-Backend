package de.hs_esslingen.besy.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.models.Order;
import jakarta.persistence.LockModeType;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findTopByPrimaryCostCenterIdAndBookingYearOrderByAutoIndexDesc(String primaryCostCenterId,
            String bookingYear);

    boolean existsByIdAndStatusNot(Long id, OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);
}
