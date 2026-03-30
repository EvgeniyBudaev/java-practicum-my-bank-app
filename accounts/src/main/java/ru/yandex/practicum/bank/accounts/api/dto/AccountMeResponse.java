package ru.yandex.practicum.bank.accounts.api.dto;

import java.time.LocalDate;

public record AccountMeResponse(String name, LocalDate birthdate, long sum) {
}
