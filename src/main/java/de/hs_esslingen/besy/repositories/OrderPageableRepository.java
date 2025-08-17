package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.Order;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderPageableRepository extends PagingAndSortingRepository<Order, Long>, JpaSpecificationExecutor<Order> {
}
