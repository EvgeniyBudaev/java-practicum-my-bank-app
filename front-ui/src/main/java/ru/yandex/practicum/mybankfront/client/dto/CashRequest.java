package ru.yandex.practicum.mybankfront.client.dto;

import ru.yandex.practicum.mybankfront.controller.dto.CashAction;

public record CashRequest(int value, CashAction action) {
}
