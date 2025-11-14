package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.ItemRequestDTO;
import de.hs_esslingen.besy.dtos.response.ItemResponseDTO;
import de.hs_esslingen.besy.exceptions.EntityAlreadyExistsException;
import de.hs_esslingen.besy.mappers.request.ItemRequestMapper;
import de.hs_esslingen.besy.mappers.response.ItemResponseMapper;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.ItemId;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Vat;
import de.hs_esslingen.besy.repositories.ItemRepository;
import de.hs_esslingen.besy.repositories.OrderRepository;
import de.hs_esslingen.besy.repositories.VatRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@AllArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final VatRepository vatRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemResponseMapper itemResponseMapper;

    public ResponseEntity<List<ItemResponseDTO>> getItemsOfOrder(Long orderId) {
        List<Item> items = itemRepository.findByOrder_Id(orderId);
        List<ItemResponseDTO> itemResponseDTOS = itemResponseMapper.toDto(items);
        return ResponseEntity.ok(itemResponseDTOS);
    }

    public ResponseEntity<List<ItemResponseDTO>> createItemsOfOrder(Long orderId, List<ItemRequestDTO> dto) {
        List<Item> items = itemRequestMapper.toEntity(dto);
        Order order = orderRepository.getReferenceById(orderId);

        // Server-side id generation
        int largestItemId = itemRepository.findByOrder_Id(orderId).size();
        AtomicInteger itemIdCounter = new AtomicInteger(largestItemId);

        items.forEach(item -> {
            int newItemId = itemIdCounter.incrementAndGet();
            ItemId itemId = new ItemId(orderId,  newItemId);
            Vat vat = vatRepository.getReferenceById(item.getVatValue());

            item.setId(itemId);
            item.setVat(vat);
            item.setMigratedToInsy(false);
            item.setOrder(order);
        });
        List<Item> itemsPersisted = itemRepository.saveAll(items);
        List<ItemResponseDTO> itemResponseDTOS = itemResponseMapper.toDto(itemsPersisted);
        return ResponseEntity.ok(itemResponseDTOS);
    }

    @Transactional
    public ResponseEntity<String> deleteItemsOfOrder(Long orderId, Integer itemId) {
        itemRepository.deleteItemByOrderIdAndItemId(orderId, itemId);
        return ResponseEntity.noContent().build();
    }

    public boolean existsItemOfOrder(Long orderId, Integer itemId) {
        return itemRepository.existsByItemIdAndOrderId(itemId, orderId);
    }
}
