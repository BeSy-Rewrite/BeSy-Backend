package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.ItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, ItemId> {
    List<Item> findByOrder_Id(Long orderOrderId);

    boolean existsByItemIdAndOrderId(Integer itemId, Long orderId);
}