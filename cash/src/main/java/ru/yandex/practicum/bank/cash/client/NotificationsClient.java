package ru.yandex.practicum.bank.cash.client;

import ru.yandex.practicum.bank.cash.service.NotificationEvent;

public interface NotificationsClient {
    void send(NotificationEvent event);
}
