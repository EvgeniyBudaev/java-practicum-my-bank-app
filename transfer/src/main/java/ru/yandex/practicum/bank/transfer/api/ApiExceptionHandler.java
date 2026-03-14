package ru.yandex.practicum.bank.transfer.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.bank.transfer.api.dto.ApiErrorResponse;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final ObjectMapper objectMapper;

    public ApiExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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
    public ResponseEntity<ApiErrorResponse> handleDownstream(WebClientResponseException e) {
        if (e.getStatusCode().value() == 409) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiErrorResponse(tryParseErrors(e), "Conflict"));
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiErrorResponse(
                        List.of("Ошибка при обращении к сервису: " + e.getStatusCode()),
                        "Downstream error"));
    }

    private List<String> tryParseErrors(WebClientResponseException e) {
        String body = e.getResponseBodyAsString();
        if (body == null || body.isBlank()) {
            return List.of("Конфликт при выполнении операции");
        }
        try {
            ApiErrorResponse parsed = objectMapper.readValue(body, ApiErrorResponse.class);
            if (parsed.errors() != null && !parsed.errors().isEmpty()) {
                return parsed.errors();
            }
            if (parsed.message() != null && !parsed.message().isBlank()) {
                return List.of(parsed.message());
            }
        } catch (Exception ignore) {
        }
        return List.of(body);
    }

}
