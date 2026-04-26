package ru.yandex.practicum.mybankfront.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bank.gateway")
public class GatewayProperties {

    private String baseUrl = "http://localhost:8081";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
