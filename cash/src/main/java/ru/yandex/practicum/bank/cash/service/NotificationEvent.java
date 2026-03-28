package ru.yandex.practicum.bank.cash.service;

import java.time.OffsetDateTime;

public record NotificationEvent(
        String type,
        long amount,
        String actorLogin,
        String targetLogin,
        OffsetDateTime occurredAt
) {
}
