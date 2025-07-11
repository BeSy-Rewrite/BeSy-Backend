package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.ItemResponseDTO;
import de.hs_esslingen.besy.mapper.ItemResponseMapper;
import de.hs_esslingen.besy.model.Item;
import de.hs_esslingen.besy.repository.ItemRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemResponseMapper itemResponseMapper;

    public ResponseEntity<List<ItemResponseDTO>> getAllItems() {
        List<Item> items = itemRepository.findAll();
        List<ItemResponseDTO> itemResponseDTOS = itemResponseMapper.toDto(items);
        return ResponseEntity.ok(itemResponseDTOS);
    }

    public ResponseEntity<List<ItemResponseDTO>> getItemsOfOrder(Long orderId) {
        List<Item> items = itemRepository.findByOrder_OrderId(orderId);
        List<ItemResponseDTO> itemResponseDTOS = itemResponseMapper.toDto(items);
        return ResponseEntity.ok(itemResponseDTOS);
    }
}
