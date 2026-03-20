package de.hs_esslingen.besy.services;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import de.hs_esslingen.besy.dtos.insy.InsyItemRequestDTO;
import de.hs_esslingen.besy.dtos.insy.InsyOrderRequestDTO;
import de.hs_esslingen.besy.enums.VatType;
import de.hs_esslingen.besy.models.CostCenter;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.models.User;
import de.hs_esslingen.besy.repositories.CostCenterRepository;
import de.hs_esslingen.besy.repositories.ItemRepository;
import de.hs_esslingen.besy.repositories.OrderRepository;
import de.hs_esslingen.besy.repositories.SupplierRepository;
import de.hs_esslingen.besy.repositories.UserRepository;

@Service
public class InsyService {

        private final RestClient oAuthRestClient;
        private final RestClient plainRestClient;
        private final OrderRepository orderRepository;
        private final SupplierRepository supplierRepository;
        private final CostCenterRepository costCenterRepository;
        private final UserRepository userRepository;
        private final ItemRepository itemRepository;
        private final OrderPDFService pdfService;

        @Value("${insy.api.base-url}")
        private String insyBaseUrl;

        @Value("${insy.api.orders-url}")
        private String insyOrdersUrl;

        @Value("${insy.api.orders.password}")
        private String password;

        @Value("${insy.api.orders.username}")
        private String username;

        @Value("${insy.api.client.name}")
        private String insyClientName;

        @Value("${insy.api.client.authorization.protocol}")
        private String authProtocol;

        // Constructor with @Qualifier annotations to specify which RestClient bean to
        // inject
        // Lombok's @RequiredArgsConstructor cannot be used here due to the need for
        // @Qualifier, so we define the constructor manually
        public InsyService(@Qualifier("oauthRestClient") RestClient oAuthRestClient,
                        @Qualifier("plainRestClient") RestClient plainRestClient, OrderRepository orderRepository,
                        SupplierRepository supplierRepository, CostCenterRepository costCenterRepository,
                        UserRepository userRepository, ItemRepository itemRepository, OrderPDFService pdfService) {
                this.oAuthRestClient = oAuthRestClient;
                this.plainRestClient = plainRestClient;
                this.orderRepository = orderRepository;
                this.supplierRepository = supplierRepository;
                this.costCenterRepository = costCenterRepository;
                this.userRepository = userRepository;
                this.itemRepository = itemRepository;
                this.pdfService = pdfService;
        }

        public ResponseEntity<String> createOrder(Long orderId) {

                Order order = orderRepository.findById(orderId).get();
                Supplier supplier = supplierRepository.findById(order.getSupplierId()).get();
                CostCenter costCenter = costCenterRepository.findById(order.getPrimaryCostCenterId()).get();
                User user = userRepository.findById(order.getOwnerId()).get();
                List<Item> items = itemRepository.findByOrder_Id(orderId);

                List<InsyItemRequestDTO> requestItems = items
                                .stream()
                                .flatMap(item -> java.util.stream.IntStream
                                                .range(0, Math.toIntExact(item.getQuantity()))
                                                .mapToObj(i -> {
                                                        InsyItemRequestDTO requestItem = new InsyItemRequestDTO();
                                                        requestItem.setItemId(item.getItemId());
                                                        BigDecimal grossPrice = item.getVatType() == VatType.brutto
                                                                        ? item.getPricePerUnit()
                                                                        : PriceConversionService
                                                                                        .convertNetPriceToGrossPrice(
                                                                                                        item.getPricePerUnit(),
                                                                                                        item.getVat());
                                                        requestItem.setItemPricePerUnit(grossPrice);
                                                        requestItem.setItemName(item.getName());
                                                        return requestItem;
                                                }))
                                .toList();

                InsyOrderRequestDTO requestOrder = new InsyOrderRequestDTO();
                requestOrder.setBesyId(orderId);
                requestOrder.setOrderNumber(pdfService.generateOrderNumber(order.getPrimaryCostCenterId(),
                                order.getBookingYear(), order.getAutoIndex()));
                requestOrder.setOrderCreatedDate(order.getCreatedDate());
                requestOrder.setSupplierName(supplier.getName());
                requestOrder.setDescription(order.getContentDescription());
                requestOrder.setCostCenter(costCenter.getId() + " - " + costCenter.getName());
                requestOrder.setUserName(user.getName() + " " + user.getSurname());
                requestOrder.setOrderQuotePrice(order.getQuotePrice());
                requestOrder.setItems(requestItems);

                String response = (authProtocol.equals("oauth2") ? oAuthRestClient : plainRestClient)
                                .post()
                                .uri(insyBaseUrl + insyOrdersUrl)
                                .attributes(authProtocol.equals("oauth2") ? clientRegistrationId(insyClientName)
                                                : clientRegistrationId("NULL"))
                                .header("Authorization", authProtocol.equals("basic") ? getAuthHeader() : null)
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
