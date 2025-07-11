package de.hs_esslingen.besy.repository;

import de.hs_esslingen.besy.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByOwnerUserName(String ownerUserName);
}
