package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private static final String VALIDATION_ERROR = "Ошибка валидации";
    private static final String NOT_FOUND_ERROR = "Объект не найден";
    private static final String SERVER_ERROR = "Внутренняя ошибка сервера";

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationException(ValidationException ex) {
        log.warn("{}: {}", VALIDATION_ERROR, ex.getMessage());
        return Map.of(
                "error", VALIDATION_ERROR,
                "message", ex.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFoundException(NotFoundException ex) {
        log.warn("{}: {}", NOT_FOUND_ERROR, ex.getMessage());
        return Map.of(
                "error", NOT_FOUND_ERROR,
                "message", ex.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        log.warn("{}: {}", VALIDATION_ERROR, errors);

        return Map.of(
                "error", VALIDATION_ERROR,
                "errors", errors
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleInternalError(Exception ex) {
        log.error("{}: {}", SERVER_ERROR, ex.getMessage(), ex);
        return Map.of(
                "error", SERVER_ERROR,
                "message", "Произошла непредвиденная ошибка"
        );
    }
}