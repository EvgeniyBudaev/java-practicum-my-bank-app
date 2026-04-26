package ru.yandex.practicum.bank.cash.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.cash.api.dto.CashAction;
import ru.yandex.practicum.bank.cash.client.AccountsClient;
import ru.yandex.practicum.bank.cash.client.NotificationsClient;
import ru.yandex.practicum.bank.common.dto.NotificationEvent;

import java.time.OffsetDateTime;

@Slf4j
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
            log.info("Deposit request: login={}, amount={}", login, value);
            circuitBreakerFactory.create("accounts-deposit").run(
                    () -> {
                        accountsClient.deposit(login, value);
                        return null;
                    });
            log.info("Deposit successful: login={}, amount={}", login, value);
            sendNotification(new NotificationEvent("CASH_IN", value, login, null, OffsetDateTime.now()));
        } else {
            log.info("Withdrawal request: login={}, amount={}", login, value);
            try {
                circuitBreakerFactory.create("accounts-withdraw").run(
                        () -> {
                            accountsClient.withdraw(login, value);
                            return null;
                        });
                log.info("Withdrawal successful: login={}, amount={}", login, value);
                sendNotification(new NotificationEvent("CASH_OUT", value, login, null, OffsetDateTime.now()));
            } catch (RuntimeException e) {
                log.warn("Withdrawal failed: login={}, amount={}, reason={}", login, value, e.getMessage());
                throw e;
            }
        }
    }

    private void sendNotification(NotificationEvent event) {
        try {
            notificationsClient.send(event);
        } catch (Exception e) {
            log.warn("Failed to send notification: type={}, login={}, error={}",
                    event.type(), event.actorLogin(), e.getMessage());
        }
    }
}
