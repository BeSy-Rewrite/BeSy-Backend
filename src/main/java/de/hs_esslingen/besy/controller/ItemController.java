package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.dto.ItemResponseDTO;
import de.hs_esslingen.besy.service.ItemService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<List<ItemResponseDTO>> getAllItems() {
        return itemService.getAllItems();
    }

    @GetMapping
    @RequestMapping("/{order-id}")
    public ResponseEntity<List<ItemResponseDTO>> getAllItemsOfOrder(@PathVariable("order-id") Long orderId) {
        return itemService.getItemsOfOrder(orderId);
    }
}
