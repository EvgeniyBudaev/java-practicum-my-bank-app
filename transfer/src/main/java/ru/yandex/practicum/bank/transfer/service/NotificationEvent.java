package ru.yandex.practicum.bank.transfer.service;

import java.time.OffsetDateTime;

public record NotificationEvent(
        String type,
        long amount,
        String actorLogin,
        String targetLogin,
        OffsetDateTime occurredAt
) {
}
