package de.hs_esslingen.besy.repository;

import de.hs_esslingen.besy.model.Item;
import de.hs_esslingen.besy.model.ItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, ItemId> {
    List<Item> findByOrder_OrderId(Long orderOrderId);
}