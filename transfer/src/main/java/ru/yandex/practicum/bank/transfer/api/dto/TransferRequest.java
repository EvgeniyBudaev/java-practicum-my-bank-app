package ru.yandex.practicum.bank.transfer.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransferRequest(
        @Positive long value,
        @NotBlank String login
) {
}
