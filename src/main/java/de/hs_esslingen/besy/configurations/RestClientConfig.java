package de.hs_esslingen.besy.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient insyClient(RestClient.Builder restClientBuilder, @Value("${insy.api.base-url}") String URL) {
        return restClientBuilder
                .baseUrl(URL)
                .build();
    }

}
