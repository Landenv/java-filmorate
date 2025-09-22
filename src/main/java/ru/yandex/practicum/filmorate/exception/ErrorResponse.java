package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
public class ErrorResponse {
    private final String error;
    private final String message;
    private final LocalDateTime timestamp;
    private final List<String> errors;

    public ErrorResponse(String error, String message) {
        this(error, message, Collections.emptyList());
    }

    // Дополнительный конструктор для списка ошибок
    public ErrorResponse(String error, List<String> errors) {
        this(error, "", errors);
    }

    // Приватный полный конструктор
    private ErrorResponse(String error, String message, List<String> errors) {
        if (error == null) {
            throw new IllegalArgumentException("Error field cannot be null");
        }
        this.error = error;
        this.message = message != null ? message : "";
        this.timestamp = LocalDateTime.now();
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }

    // Фабричные методы для удобства
    public static ErrorResponse withMessage(String error, String message) {
        return new ErrorResponse(error, message);
    }

    public static ErrorResponse withErrors(String error, List<String> errors) {
        return new ErrorResponse(error, errors);
    }
}