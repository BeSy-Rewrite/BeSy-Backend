package de.hs_esslingen.besy.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

@Configuration
public class RestClientConfig {

    @Bean("oauthRestClient")
    public RestClient oauthRestClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        OAuth2ClientHttpRequestInterceptor requestInterceptor =
                new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        return RestClient.builder()
                .requestInterceptor(requestInterceptor)
                .build();
    }


    @Bean("plainRestClient")
    public RestClient plainRestClient() {
        return RestClient.builder()
                .build();
    }

    @Bean("loggingRestClient")
    public static RestClient createLoggingClient() {
        return RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    System.out.println("=== Outgoing Request ===");
                    System.out.println(request.getMethod() + " " + request.getURI());
                    request.getHeaders().forEach((k, v) ->
                            System.out.println(k + ": " + String.join(",", v))
                    );
                    if (body != null && body.length > 0) {
                        System.out.println("Body: " + new String(body, StandardCharsets.UTF_8));
                    }
                    System.out.println("========================");
                    return execution.execute(request, body);
                })
                .build();
    }
}
