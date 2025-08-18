package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.request.InsyItemRequestDTO;
import de.hs_esslingen.besy.dtos.request.InsyOrderRequestDTO;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@RestController
@RequestMapping("${api.prefix}/insy")
public class InsyController {

    private final RestClient restClient;

    @Value("${insy.api.base-url}")
    private String insyBaseUrl;

    @Value("${insy.api.orders-url}")
    private String insyOrdersUrl;

    // Do not use @ALlArgsConstructor, since this will break insyBaseUrl & insyOrdersUrl
    public InsyController(RestClient restClient) {
        this.restClient = restClient;
    }

    @PostMapping
    public ResponseEntity<String> postOrderToInsy(){


        InsyOrderRequestDTO orderRequestDTO = new InsyOrderRequestDTO();
        InsyItemRequestDTO insyItemRequestDTO = new InsyItemRequestDTO();

        insyItemRequestDTO.setItemId(1);
        insyItemRequestDTO.setItemName("Grüßli von BESY =D");
        insyItemRequestDTO.setItemPricePerUnit(42);

        orderRequestDTO.setOrderId(1);
        orderRequestDTO.setOrderCreatedDate(LocalDateTime.now());
        orderRequestDTO.setSupplierName("Harro Höfliger");
        orderRequestDTO.setCostCenterName("Kostenstelle 123");
        orderRequestDTO.setUserName("Sanderino");
        orderRequestDTO.setOrderQuotePrice(420);
        orderRequestDTO.setItems(List.of(insyItemRequestDTO));

        Object response = restClient
                .post()
                .uri(insyBaseUrl + insyOrdersUrl)
                .attributes(clientRegistrationId("besy-for-insy-client"))
                .body(orderRequestDTO)
                .retrieve();

        System.out.println(response);

        return ResponseEntity.ok("123");
    }
}
