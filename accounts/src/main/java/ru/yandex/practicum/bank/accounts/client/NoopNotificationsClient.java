package ru.yandex.practicum.bank.accounts.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bank.security.enabled", havingValue = "false")
public class NoopNotificationsClient implements NotificationsClient {
    @Override
    public void send(NotificationEvent event) {
    }
}
