package ru.yandex.practicum.bank.accounts.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EditAccountRequest(
        @NotBlank String name,
        @NotNull LocalDate birthdate
) {
}
