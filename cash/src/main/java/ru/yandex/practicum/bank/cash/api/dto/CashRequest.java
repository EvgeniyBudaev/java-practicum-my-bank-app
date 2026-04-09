package ru.yandex.practicum.bank.cash.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CashRequest(@Positive long value, @NotNull CashAction action) {
}
