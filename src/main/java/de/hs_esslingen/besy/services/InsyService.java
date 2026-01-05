package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.insy.InsyItemRequestDTO;
import de.hs_esslingen.besy.dtos.insy.InsyOrderRequestDTO;
import de.hs_esslingen.besy.models.*;
import de.hs_esslingen.besy.repositories.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
public class InsyService {

    private final RestClient restClient;
    private final OrderRepository orderRepository;
    private final SupplierRepository supplierRepository;
    private final CostCenterRepository costCenterRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Value("${insy.api.base-url}")
    private String insyBaseUrl;

    @Value("${insy.api.orders-url}")
    private String insyOrdersUrl;

    @Value("${insy.api.orders.password}")
    private String password;

    @Value("${insy.api.orders.username}")
    private String username;

    // Do not use @ALlArgsConstructor, since this will break insyBaseUrl & insyOrdersUrl
    public InsyService(@Qualifier("plainRestClient")RestClient restClient, OrderRepository orderRepository, SupplierRepository supplierRepository, CostCenterRepository costCenterRepository, UserRepository userRepository, ItemRepository itemRepository) {
        this.restClient = restClient;
        this.orderRepository = orderRepository;
        this.supplierRepository = supplierRepository;
        this.costCenterRepository = costCenterRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    public ResponseEntity<String> createOrder(Long orderId) {

        Order order = orderRepository.findById(orderId).get();
        Supplier supplier = supplierRepository.findById(order.getSupplierId()).get();
        CostCenter costCenter = costCenterRepository.findById(order.getPrimaryCostCenterId()).get();
        User user = userRepository.findById(order.getOwnerId()).get();
        List<Item> items = itemRepository.findByOrder_Id(orderId);

        List<InsyItemRequestDTO> requestItems = items
                .stream()
                .flatMap(item -> java.util.stream.IntStream.range(0, Math.toIntExact(item.getQuantity()))
                        .mapToObj(i -> {
                            InsyItemRequestDTO requestItem = new InsyItemRequestDTO();
                            requestItem.setItemId(item.getItemId());
                            requestItem.setItemPricePerUnit(item.getPricePerUnit());
                            requestItem.setItemName(item.getName());
                            return requestItem;
                        })
                )
                .toList();

        InsyOrderRequestDTO requestOrder = new InsyOrderRequestDTO();
        requestOrder.setBesyId(orderId);
        requestOrder.setOrderNumber(OrderPDFService.generateOrderNumber(order.getPrimaryCostCenterId(), order.getBookingYear(), order.getAutoIndex()));
        requestOrder.setOrderCreatedDate(order.getCreatedDate());
        requestOrder.setSupplierName(supplier.getName());
        requestOrder.setDescription(order.getContentDescription());
        requestOrder.setCostCenter(costCenter.getId() + " - " + costCenter.getName());
        requestOrder.setUserName(user.getName() + " " + user.getSurname());
        requestOrder.setOrderQuotePrice(order.getQuotePrice());
        requestOrder.setItems(requestItems);


        String response = restClient
                .post()
                .uri(insyBaseUrl + insyOrdersUrl)
                .header("Authorization", getAuthHeader())
                .header("Content-Type", "application/json")
                .body(List.of(requestOrder))
                .retrieve()
                .body(String.class);

        // Set all items as "migrated to insy"
        items.forEach(item -> {
            item.setMigratedToInsy(true);
        });
        itemRepository.saveAll(items);

        return ResponseEntity.ok(response);
    }


    public String getAuthHeader() {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth);
        return authHeader;
    }

}
