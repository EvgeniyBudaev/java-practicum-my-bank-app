package ru.yandex.practicum.bank.common.dto;

import java.time.OffsetDateTime;

public record NotificationEvent(
        String type,
        long amount,
        String actorLogin,
        String targetLogin,
        OffsetDateTime occurredAt
) {
}
