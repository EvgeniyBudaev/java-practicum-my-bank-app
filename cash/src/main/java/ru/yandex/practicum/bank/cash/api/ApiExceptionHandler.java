package ru.yandex.practicum.bank.cash.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.bank.cash.api.dto.ApiErrorResponse;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return new ApiErrorResponse(errors, "Validation error");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBusiness(IllegalArgumentException e) {
        return new ApiErrorResponse(List.of(e.getMessage()), "Bad request");
    }

    @ExceptionHandler(WebClientResponseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleDownstream(WebClientResponseException e) {
        return new ApiErrorResponse(List.of("Ошибка при обращении к сервису: " + e.getStatusCode()), "Downstream error");
    }
}
