package ru.yandex.practicum.bank.accounts.api.dto;

import jakarta.validation.constraints.Positive;

public record BalanceChangeRequest(@Positive long amount) {
}
