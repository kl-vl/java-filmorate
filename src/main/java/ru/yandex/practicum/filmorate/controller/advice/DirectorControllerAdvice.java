package ru.yandex.practicum.filmorate.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ru.yandex.practicum.filmorate.controller.DirectorController;
import ru.yandex.practicum.filmorate.dto.error.ErrorResponse;
import ru.yandex.practicum.filmorate.exception.DirectorCreateFailed;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(assignableTypes = DirectorController.class)
public class DirectorControllerAdvice {

    @ExceptionHandler(DirectorNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDirectorNotFound(DirectorNotFoundException ex, WebRequest request) {

        log.warn("Director not found in {}: {}", request.getDescription(false), ex.getMessage());

        return new ErrorResponse(ex.getMessage(), "DIRECTOR_NOT_FOUND");
    }

    @ExceptionHandler(DirectorCreateFailed.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDirectorCreateFailed(DirectorCreateFailed ex, WebRequest request) {
        log.warn("Director create failed in {}: {}", request.getDescription(false), ex.getMessage());

        return new ErrorResponse(ex.getMessage(), "DIRECTOR_CREATE_FAILED");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> Optional.ofNullable(fieldError.getDefaultMessage()).orElse("Invalid value")
                ));

        log.warn("Method argument validation error in {} : {}", request.getDescription(false), errors);

        return new ErrorResponse("Validation failed", "DIRECTOR_VALIDATION_ERROR", errors);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Internal error in {}: {}", request.getDescription(true), ex.getMessage(), ex);

        return new ErrorResponse("Internal server error", "INTERNAL_ERROR", Map.of(ex.getClass().getSimpleName(), ex.getMessage()));
    }

}