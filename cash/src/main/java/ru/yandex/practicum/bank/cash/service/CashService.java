package ru.yandex.practicum.bank.cash.service;

import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.cash.api.dto.CashAction;
import ru.yandex.practicum.bank.cash.client.AccountsClient;
import ru.yandex.practicum.bank.cash.client.NotificationsClient;

import java.time.OffsetDateTime;

@Service
public class CashService {

    private final AccountsClient accountsClient;
    private final NotificationsClient notificationsClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public CashService(AccountsClient accountsClient,
                       NotificationsClient notificationsClient,
                       CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.accountsClient = accountsClient;
        this.notificationsClient = notificationsClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public void apply(String login, long value, CashAction action) {
        if (value <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше 0");
        }

        if (action == CashAction.PUT) {
            circuitBreakerFactory.create("accounts-deposit").run(
                    () -> {
                        accountsClient.deposit(login, value);
                        return null;
                    });
            sendNotification(new NotificationEvent("CASH_IN", value, login, null, OffsetDateTime.now()));
        } else {
            circuitBreakerFactory.create("accounts-withdraw").run(
                    () -> {
                        accountsClient.withdraw(login, value);
                        return null;
                    });
            sendNotification(new NotificationEvent("CASH_OUT", value, login, null, OffsetDateTime.now()));
        }
    }

    private void sendNotification(NotificationEvent event) {
        circuitBreakerFactory.create("notifications").run(
                () -> {
                    notificationsClient.send(event);
                    return null;
                },
                throwable -> null);
    }
}
