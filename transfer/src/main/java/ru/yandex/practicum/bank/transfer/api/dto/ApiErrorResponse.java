package ru.yandex.practicum.bank.transfer.api.dto;

import java.util.List;

public record ApiErrorResponse(List<String> errors, String message) {
}
