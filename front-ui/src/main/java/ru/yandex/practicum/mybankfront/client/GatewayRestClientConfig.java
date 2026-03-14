package ru.yandex.practicum.mybankfront.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GatewayRestClientConfig {

    @Bean
    public RestClient gatewayRestClient(
            RestClient.Builder builder,
            @Value("${bank.gateway.base-url:http://localhost:8081}") String baseUrl
    ) {
        return builder.baseUrl(baseUrl).build();
    }
}
