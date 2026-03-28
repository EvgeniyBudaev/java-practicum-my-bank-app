package ru.yandex.practicum.bank.accounts.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@ConditionalOnProperty(name = "bank.security.enabled", havingValue = "true", matchIfMissing = true)
public class OAuthNotificationsClient implements NotificationsClient {

    private final WebClient webClient;

    public OAuthNotificationsClient(WebClient serviceWebClient) {
        this.webClient = serviceWebClient;
    }

    @Override
    public void send(NotificationEvent event) {
        webClient.post()
                .uri("http://notifications/api/notifications")
                .bodyValue(event)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
