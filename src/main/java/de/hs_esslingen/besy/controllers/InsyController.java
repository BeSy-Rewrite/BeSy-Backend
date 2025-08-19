package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.request.InsyItemRequestDTO;
import de.hs_esslingen.besy.dtos.request.InsyOrderRequestDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/insy")
public class InsyController {

    private final RestClient restClient;

    @Value("${insy.api.base-url}")
    private String insyBaseUrl;

    @Value("${insy.api.orders-url}")
    private String insyOrdersUrl;

    @Value("${insy.api.orders.password}")
    private String password;

    @Value("${insy.api.orders.username}")
    private String username;

    // Do not use @ALlArgsConstructor, since this will break insyBaseUrl & insyOrdersUrl
    public InsyController(@Qualifier("plainRestClient")RestClient restClient) {
        this.restClient = restClient;
    }

    @PostMapping
    public ResponseEntity<?> postOrderToInsy(){

        List<InsyOrderRequestDTO> orders = new ArrayList<>();
        InsyOrderRequestDTO orderRequestDTO = new InsyOrderRequestDTO();
        InsyItemRequestDTO insyItemRequestDTO = new InsyItemRequestDTO();
        InsyItemRequestDTO insyItemRequestDTO2 = new InsyItemRequestDTO();

        insyItemRequestDTO.setItemId(1);
        insyItemRequestDTO.setItemName("1. Artikel");
        insyItemRequestDTO.setItemPricePerUnit(BigDecimal.valueOf(100));

        insyItemRequestDTO2.setItemId(2);
        insyItemRequestDTO2.setItemName("2. Artikel");
        insyItemRequestDTO2.setItemPricePerUnit(BigDecimal.valueOf(320));

        orderRequestDTO.setOrderId(502);
        orderRequestDTO.setOrderCreatedDate(LocalDateTime.now());
        orderRequestDTO.setSupplierName("Harro Höfliger");
        orderRequestDTO.setCostCenterName("Kostenstelle 123");
        orderRequestDTO.setUserName("Sanderino");
        orderRequestDTO.setOrderQuotePrice(BigDecimal.valueOf(420.69));
        orderRequestDTO.setItems(List.of(insyItemRequestDTO, insyItemRequestDTO2));
        orders.add(orderRequestDTO);


        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth);


        String response = restClient
                .post()
                .uri(insyBaseUrl + insyOrdersUrl)
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .body(orders)
                .retrieve()
                .body(String.class);

        System.out.println(response);

        return ResponseEntity.ok(response);
    }
}
