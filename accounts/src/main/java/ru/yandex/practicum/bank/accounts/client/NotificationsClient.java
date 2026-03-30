package ru.yandex.practicum.bank.accounts.client;

public interface NotificationsClient {
    void send(NotificationEvent event);
}
