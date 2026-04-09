package ru.yandex.practicum.mybankfront.client.dto;

import java.time.LocalDate;

public record AccountMeResponse(String name, LocalDate birthdate, long sum) {
}
