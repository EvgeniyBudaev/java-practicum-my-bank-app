package ru.yandex.practicum.bank.cash.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bank.security.enabled", havingValue = "false")
public class NoopAccountsClient implements AccountsClient {
    @Override
    public void deposit(String login, long amount) {
        throw new IllegalStateException("AccountsClient is disabled");
    }

    @Override
    public void withdraw(String login, long amount) {
        throw new IllegalStateException("AccountsClient is disabled");
    }
}
