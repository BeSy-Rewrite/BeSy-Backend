package de.hs_esslingen.besy.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.ItemId;

public interface ItemRepository extends JpaRepository<Item, ItemId> {
    List<Item> findByOrder_Id(Long orderOrderId);

    boolean existsByItemIdAndOrderId(Integer itemId, Long orderId);

    void deleteItemByOrderIdAndItemId(Long orderId, Integer itemId);

    @Query("SELECT MAX(i.itemId) FROM Item i WHERE i.order.id = :orderId")
    Optional<Integer> findTopItemIdByOrderIdOrderByItemIdDesc(Long orderId);
}
