package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.ItemRequestDTO;
import de.hs_esslingen.besy.dtos.response.ItemResponseDTO;
import de.hs_esslingen.besy.mappers.request.ItemRequestMapper;
import de.hs_esslingen.besy.mappers.response.ItemResponseMapper;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.ItemId;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Vat;
import de.hs_esslingen.besy.repositories.ItemRepository;
import de.hs_esslingen.besy.repositories.OrderRepository;
import de.hs_esslingen.besy.repositories.VatRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

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

        items.forEach(item -> {
            if(itemRepository.existsByItemIdAndOrderId(item.getItemId(), orderId)) throw new RuntimeException("Zugehöriger Artikel mit Artikelnummer " + item.getItemId() + " existiert bereits.");

            ItemId itemId = new ItemId(orderId, item.getItemId());
            Vat vat = vatRepository.getReferenceById(item.getVatValue());

            item.setId(itemId);
            item.setVat(vat);
            item.setOrder(order);
        });
        List<Item> itemsPersisted = itemRepository.saveAll(items);
        List<ItemResponseDTO> itemResponseDTOS = itemResponseMapper.toDto(itemsPersisted);
        return ResponseEntity.ok(itemResponseDTOS);
    }
}
