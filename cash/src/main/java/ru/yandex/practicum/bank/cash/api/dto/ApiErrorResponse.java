package ru.yandex.practicum.bank.cash.api.dto;

import java.util.List;

public record ApiErrorResponse(List<String> errors, String message) {
}
