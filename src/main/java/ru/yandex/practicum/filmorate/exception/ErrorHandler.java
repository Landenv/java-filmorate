package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private static final String VALIDATION_ERROR = "Ошибка валидации";
    private static final String NOT_FOUND_ERROR = "Объект не найден";
    private static final String SERVER_ERROR = "Внутренняя ошибка сервера";

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(ValidationException exception) {
        log.warn("Validation error: {}", exception.getMessage());
        return ErrorResponse.withMessage(VALIDATION_ERROR, exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException exception) {
        log.warn("{}: {}", NOT_FOUND_ERROR, exception.getMessage());
        return new ErrorResponse(NOT_FOUND_ERROR, exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        log.warn("Validation errors: {}", errors);
        return ErrorResponse.withErrors(VALIDATION_ERROR, errors);
    }

    @ExceptionHandler(Exception.class) // Явно указали тип
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalError(Exception exception) {
        log.error("{}: {}", SERVER_ERROR, exception.getMessage(), exception); // Теперь точно сработает
        return new ErrorResponse(SERVER_ERROR, "Внутренняя ошибка сервера");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleReviewValidationException(ReviewValidationException exception) {
        log.warn("Review validation error: {}", exception.getMessage());
        return ErrorResponse.withMessage(VALIDATION_ERROR, exception.getMessage());
    }

}