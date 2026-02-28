package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.ItemRequestDTO;
import de.hs_esslingen.besy.dtos.response.ItemResponseDTO;
import de.hs_esslingen.besy.enums.PreferredList;
import de.hs_esslingen.besy.enums.VatType;
import de.hs_esslingen.besy.mappers.request.ItemRequestMapper;
import de.hs_esslingen.besy.mappers.response.ItemResponseMapper;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.ItemId;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Vat;
import de.hs_esslingen.besy.repositories.ItemRepository;
import de.hs_esslingen.besy.repositories.OrderRepository;
import de.hs_esslingen.besy.repositories.VatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private VatRepository vatRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @Mock
    private ItemResponseMapper itemResponseMapper;

    @InjectMocks
    private ItemService itemService;

    private ItemRequestDTO requestDto;
    private Item item;
    private ItemResponseDTO responseDto;
    private Order order;
    private Vat vat;

    @BeforeEach
    void setUp() {
        requestDto = new ItemRequestDTO(
                "Item A",
                BigDecimal.valueOf(10.5),
                2L,
                "pcs",
                "A-1",
                "AN-001",
                "Comment",
                BigDecimal.valueOf(19),
                PreferredList.RZ,
                "PL-1",
                VatType.netto
        );

        item = new Item();
        item.setName("Item A");
        item.setPricePerUnit(BigDecimal.valueOf(10.5));
        item.setQuantity(2L);
        item.setQuantityUnit("pcs");
        item.setArticleId("A-1");
        item.setArticleNumber("AN-001");
        item.setComment("Comment");
        item.setVatValue(BigDecimal.valueOf(19));
        item.setVatType(VatType.netto);
        item.setPreferredList(PreferredList.RZ);
        item.setPreferredListNumber("PL-1");

        responseDto = new ItemResponseDTO(
                1,
                "Item A",
                BigDecimal.valueOf(10.5),
                2L,
                "pcs",
                "A-1",
                "AN-001",
                "Comment",
                null,
                PreferredList.RZ,
                "PL-1",
                VatType.netto
        );

        order = new Order();
        order.setId(100L);

        vat = new Vat();
        vat.setValue(BigDecimal.valueOf(19));
    }

    @Test
    void should_return_items_of_order_as_dtos() {
        Long orderId = 100L;
        List<Item> items = List.of(item);
        List<ItemResponseDTO> dtos = List.of(responseDto);

        when(itemRepository.findByOrder_Id(orderId)).thenReturn(items);
        when(itemResponseMapper.toDto(items)).thenReturn(dtos);

        ResponseEntity<List<ItemResponseDTO>> response = itemService.getItemsOfOrder(orderId);

        assertSame(dtos, response.getBody());
        verify(itemRepository).findByOrder_Id(orderId);
        verify(itemResponseMapper).toDto(items);
        verify(itemRepository, never()).delete(any(Item.class));
        verify(itemRepository, never()).deleteAll();
    }

    @Test
    void should_create_items_assign_ids_and_return_dtos() {
        Long orderId = 100L;
        List<ItemRequestDTO> requestDtos = List.of(requestDto, requestDto);
        Item item2 = new Item();
        item2.setName("Item B");
        item2.setPricePerUnit(BigDecimal.valueOf(20.0));
        item2.setQuantity(1L);
        item2.setQuantityUnit("pcs");
        item2.setArticleId("B-1");
        item2.setArticleNumber("AN-002");
        item2.setComment("Comment 2");
        item2.setVatValue(BigDecimal.valueOf(19));
        item2.setVatType(VatType.netto);
        item2.setPreferredList(PreferredList.RZ);
        item2.setPreferredListNumber("PL-2");
        List<Item> mappedItems = List.of(item, item2);

        when(itemRequestMapper.toEntity(requestDtos)).thenReturn(mappedItems);
        when(orderRepository.getReferenceById(orderId)).thenReturn(order);
        when(itemRepository.findByOrder_Id(orderId)).thenReturn(List.of());
        when(vatRepository.getReferenceById(item.getVatValue())).thenReturn(vat);
        when(vatRepository.getReferenceById(item2.getVatValue())).thenReturn(vat);
        when(itemRepository.saveAll(mappedItems)).thenReturn(mappedItems);
        when(itemResponseMapper.toDto(mappedItems)).thenReturn(List.of(responseDto, responseDto));

        ResponseEntity<List<ItemResponseDTO>> response = itemService.createItemsOfOrder(orderId, requestDtos);

        assertEquals(List.of(responseDto, responseDto), response.getBody());

        ArgumentCaptor<List<Item>> captor = ArgumentCaptor.forClass(List.class);
        verify(itemRepository).saveAll(captor.capture());
        List<Item> savedItems = captor.getValue();
        Item saved = savedItems.get(0);
        Item saved2 = savedItems.get(1);

        assertEquals(new ItemId(orderId, 1), saved.getId());
        assertEquals(new ItemId(orderId, 2), saved2.getId());
        assertSame(order, saved.getOrder());
        assertSame(vat, saved.getVat());
        assertEquals(false, saved.getMigratedToInsy());
        assertSame(order, saved2.getOrder());
        assertSame(vat, saved2.getVat());
        assertEquals(false, saved2.getMigratedToInsy());

        verify(itemRequestMapper).toEntity(requestDtos);
        verify(orderRepository).getReferenceById(orderId);
        verify(itemRepository).findByOrder_Id(orderId);
        //verify(vatRepository).getReferenceById(item.getVatValue());
        verify(itemResponseMapper).toDto(mappedItems);
    }

    @Test
    void should_delete_items_of_order_and_return_no_content() {
        Long orderId = 100L;
        Integer itemId = 1;

        ResponseEntity<String> response = itemService.deleteItemsOfOrder(orderId, itemId);

        assertEquals(204, response.getStatusCode().value());
        verify(itemRepository).deleteItemByOrderIdAndItemId(orderId, itemId);
        verify(itemRepository, never()).delete(any(Item.class));
        verify(itemRepository, never()).deleteAll();
    }

    @Test
    void should_return_true_when_item_exists() {
        when(itemRepository.existsByItemIdAndOrderId(1, 100L)).thenReturn(true);

        boolean result = itemService.existsItemOfOrder(100L, 1);

        assertTrue(result);
        verify(itemRepository).existsByItemIdAndOrderId(1, 100L);
    }

    @Test
    void should_return_false_when_item_does_not_exist() {
        when(itemRepository.existsByItemIdAndOrderId(2, 100L)).thenReturn(false);

        boolean result = itemService.existsItemOfOrder(100L, 2);

        assertFalse(result);
        verify(itemRepository).existsByItemIdAndOrderId(2, 100L);
    }

    @Test
    void should_persist_article_number_when_creating_item() {
        Long orderId = 100L;
        List<ItemRequestDTO> requestDtos = List.of(requestDto);
        List<Item> mappedItems = List.of(item);

        when(itemRequestMapper.toEntity(requestDtos)).thenReturn(mappedItems);
        when(orderRepository.getReferenceById(orderId)).thenReturn(order);
        when(itemRepository.findByOrder_Id(orderId)).thenReturn(List.of());
        when(vatRepository.getReferenceById(item.getVatValue())).thenReturn(vat);
        when(itemRepository.saveAll(mappedItems)).thenReturn(mappedItems);
        when(itemResponseMapper.toDto(mappedItems)).thenReturn(List.of(responseDto));

        itemService.createItemsOfOrder(orderId, requestDtos);

        ArgumentCaptor<List<Item>> captor = ArgumentCaptor.forClass(List.class);
        verify(itemRepository).saveAll(captor.capture());
        Item saved = captor.getValue().get(0);

        assertEquals("AN-001", saved.getArticleNumber());
        assertEquals("A-1", saved.getArticleId());
    }
}
