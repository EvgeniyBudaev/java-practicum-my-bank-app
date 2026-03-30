package ru.yandex.practicum.bank.transfer.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.bank.transfer.service.NotificationEvent;

@Component
@ConditionalOnProperty(name = "bank.security.enabled", havingValue = "false")
public class NoopNotificationsClient implements NotificationsClient {

    @Override
    public void send(NotificationEvent event) {
    }
}
