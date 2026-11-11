package ru.practicum.explorewithme.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private static final String BAD_REQUEST_REASON = "Запрос составлен некорректно.";
    private static final String NOT_FOUND_REASON = "Искомый объект не найден.";
    private static final String CONFLICT_REASON = "Для запрошенной операции не выполнены условия.";
    private static final String INTEGRITY_REASON = "Нарушено ограничение целостности данных.";

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBodyValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(this::describe)
                .toList();
        String message = errors.isEmpty() ? e.getMessage() : String.join("; ", errors);
        log.warn("400: {}", message);
        return new ApiError(errors, message, BAD_REQUEST_REASON, HttpStatus.BAD_REQUEST, LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleParamValidation(HandlerMethodValidationException e) {
        List<String> errors = e.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> "Параметр: " + result.getMethodParameter().getParameterName()
                                + ". Ошибка: " + error.getDefaultMessage()
                                + ". Значение: " + result.getArgument()))
                .toList();
        String message = errors.isEmpty() ? e.getMessage() : String.join("; ", errors);
        log.warn("400: {}", message);
        return new ApiError(errors, message, BAD_REQUEST_REASON, HttpStatus.BAD_REQUEST, LocalDateTime.now());
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(Exception e) {
        log.warn("400: {}", e.getMessage());
        return build(HttpStatus.BAD_REQUEST, BAD_REQUEST_REASON, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException e) {
        log.warn("404: {}", e.getMessage());
        return build(HttpStatus.NOT_FOUND, NOT_FOUND_REASON, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(ConflictException e) {
        log.warn("409: {}", e.getMessage());
        return build(HttpStatus.CONFLICT, CONFLICT_REASON, e.getMessage());
    }

    // Нарушение ограничений БД, например повторный email или имя категории
    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIntegrity(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();
        log.warn("409: {}", message);
        return build(HttpStatus.CONFLICT, INTEGRITY_REASON, message);
    }

    private ApiError build(HttpStatus status, String reason, String message) {
        return new ApiError(List.of(), message, reason, status, LocalDateTime.now());
    }

    private String describe(FieldError error) {
        return "Поле: " + error.getField()
                + ". Ошибка: " + error.getDefaultMessage()
                + ". Значение: " + error.getRejectedValue();
    }
}
