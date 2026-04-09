package ru.yandex.practicum.bank.accounts.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.bank.accounts.api.dto.ApiErrorResponse;
import ru.yandex.practicum.bank.accounts.service.NotEnoughFundsException;

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

    @ExceptionHandler(NotEnoughFundsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleNotEnough(NotEnoughFundsException e) {
        return new ApiErrorResponse(List.of(e.getMessage()), "Conflict");
    }
}
