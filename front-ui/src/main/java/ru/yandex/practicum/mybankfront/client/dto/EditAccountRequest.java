package ru.yandex.practicum.mybankfront.client.dto;

import java.time.LocalDate;

public record EditAccountRequest(String name, LocalDate birthdate) {
}
