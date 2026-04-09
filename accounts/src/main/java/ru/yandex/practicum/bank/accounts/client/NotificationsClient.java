package ru.yandex.practicum.bank.accounts.client;

import ru.yandex.practicum.bank.common.dto.NotificationEvent;

public interface NotificationsClient {
    void send(NotificationEvent event);
}
