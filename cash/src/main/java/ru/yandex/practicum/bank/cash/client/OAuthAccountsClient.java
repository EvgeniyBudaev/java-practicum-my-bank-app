package ru.yandex.practicum.bank.cash.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@ConditionalOnProperty(name = "bank.security.enabled", havingValue = "true", matchIfMissing = true)
public class OAuthAccountsClient implements AccountsClient {

    private final WebClient webClient;

    public OAuthAccountsClient(WebClient serviceWebClient) {
        this.webClient = serviceWebClient;
    }

    @Override
    public void deposit(String login, long amount) {
        webClient.post()
                .uri("http://accounts/internal/accounts/{login}/deposit", login)
                .bodyValue(new AmountDto(amount))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @Override
    public void withdraw(String login, long amount) {
        webClient.post()
                .uri("http://accounts/internal/accounts/{login}/withdraw", login)
                .bodyValue(new AmountDto(amount))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private record AmountDto(long amount) {
    }
}
