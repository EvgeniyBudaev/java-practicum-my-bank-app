package ru.yandex.practicum.mybankfront.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GatewayRestClientConfig {

    @Bean
    public RestClient gatewayRestClient(RestClient.Builder builder,
                                        GatewayProperties gatewayProperties) {
        return builder.baseUrl(gatewayProperties.getBaseUrl()).build();
    }
}
