package ru.yandex.practicum.mybankfront.client.dto;

import java.util.List;

public record ApiErrorResponse(List<String> errors, String message) {
}
