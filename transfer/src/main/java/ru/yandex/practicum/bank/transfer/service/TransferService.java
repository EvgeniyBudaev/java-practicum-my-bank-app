package ru.yandex.practicum.bank.transfer.service;

import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.transfer.client.AccountsClient;
import ru.yandex.practicum.bank.transfer.client.NotificationsClient;

import java.time.OffsetDateTime;

@Service
public class TransferService {

    private final AccountsClient accountsClient;
    private final NotificationsClient notificationsClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public TransferService(AccountsClient accountsClient,
                           NotificationsClient notificationsClient,
                           CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.accountsClient = accountsClient;
        this.notificationsClient = notificationsClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public void transfer(String fromLogin, String toLogin, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше 0");
        }
        if (toLogin == null || toLogin.isBlank()) {
            throw new IllegalArgumentException("Получатель обязателен");
        }
        if (fromLogin.equals(toLogin)) {
            throw new IllegalArgumentException("Нельзя переводить самому себе");
        }

        circuitBreakerFactory.create("accounts-withdraw").run(
                () -> {
                    accountsClient.withdraw(fromLogin, amount);
                    return null;
                });

        try {
            circuitBreakerFactory.create("accounts-deposit").run(
                    () -> {
                        accountsClient.deposit(toLogin, amount);
                        return null;
                    });
        } catch (RuntimeException e) {
            try {
                accountsClient.deposit(fromLogin, amount);
            } catch (RuntimeException ignored) {
            }
            throw e;
        }

        circuitBreakerFactory.create("notifications").run(
                () -> {
                    notificationsClient.send(new NotificationEvent(
                            "TRANSFER", amount, fromLogin, toLogin, OffsetDateTime.now()));
                    return null;
                },
                throwable -> null);
    }
}
