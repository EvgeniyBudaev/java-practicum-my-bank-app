package ru.yandex.practicum.bank.transfer.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.bank.transfer.client.AccountsClient;
import ru.yandex.practicum.bank.transfer.client.NotificationsClient;
import ru.yandex.practicum.bank.common.dto.NotificationEvent;

import java.time.OffsetDateTime;

@Slf4j
@Service
public class TransferService {

    private final AccountsClient accountsClient;
    private final NotificationsClient notificationsClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final MeterRegistry meterRegistry;

    public TransferService(AccountsClient accountsClient,
                           NotificationsClient notificationsClient,
                           CircuitBreakerFactory<?, ?> circuitBreakerFactory,
                           MeterRegistry meterRegistry) {
        this.accountsClient = accountsClient;
        this.notificationsClient = notificationsClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.meterRegistry = meterRegistry;
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

        log.info("Transfer request: from={}, to={}, amount={}", fromLogin, toLogin, amount);

        try {
            circuitBreakerFactory.create("accounts-withdraw").run(
                    () -> {
                        accountsClient.withdraw(fromLogin, amount);
                        return null;
                    });
        } catch (RuntimeException e) {
            log.warn("Transfer failed — withdrawal error: from={}, to={}, amount={}, reason={}",
                    fromLogin, toLogin, amount, e.getMessage());
            meterRegistry.counter("bank.transfer.failed", "step", "withdraw").increment();
            throw e;
        }

        try {
            circuitBreakerFactory.create("accounts-deposit").run(
                    () -> {
                        accountsClient.deposit(toLogin, amount);
                        return null;
                    });
        } catch (RuntimeException e) {
            log.error("Transfer failed — deposit error, reverting: from={}, to={}, amount={}, reason={}",
                    fromLogin, toLogin, amount, e.getMessage());
            meterRegistry.counter("bank.transfer.failed", "step", "deposit").increment();
            try {
                accountsClient.deposit(fromLogin, amount);
                log.info("Compensating deposit successful: login={}, amount={}", fromLogin, amount);
            } catch (RuntimeException compensationEx) {
                log.error("Compensating deposit failed: login={}, amount={}, reason={}",
                        fromLogin, amount, compensationEx.getMessage());
            }
            throw e;
        }

        log.info("Transfer successful: from={}, to={}, amount={}", fromLogin, toLogin, amount);

        try {
            notificationsClient.send(new NotificationEvent(
                    "TRANSFER", amount, fromLogin, toLogin, OffsetDateTime.now()));
        } catch (Exception e) {
            log.warn("Failed to send transfer notification: from={}, to={}, error={}",
                    fromLogin, toLogin, e.getMessage());
        }
    }
}
