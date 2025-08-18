package de.hs_esslingen.besy.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/insy")
public class InsyController {

    private final RestClient restClient;

    @PostMapping
    public ResponseEntity<String> postOrderToInsy(){

        Object response = restClient
                .post()
                .uri("/orders")
                .header("Authorization", "application/json")
                .retrieve()
                .body(Object.class);

        System.out.println(response);

        return ResponseEntity.ok("123");
    }
}
