package ru.yandex.practicum.filmorate.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.dto.error.ErrorResponse;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(assignableTypes = UserController.class)
public class UserControllerAdvice {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException ex, WebRequest request) {

        log.warn("User not found in {}: {}", request.getDescription(false), ex.getMessage());

        return new ErrorResponse(ex.getMessage(), "USER_NOT_FOUND");
    }

    @ExceptionHandler(UserValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(UserValidationException ex, WebRequest request) {

        log.warn("User validation error in {}: {}", request.getDescription(false), ex.getMessage());

        return new ErrorResponse(ex.getMessage(), "USER_INVALID_DATA");
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleStateException(IllegalStateException ex, WebRequest request) {

        log.warn("User state error in {}: {}", request.getDescription(false), ex.getMessage());

        return new ErrorResponse(ex.getMessage(), "USER_ILLEGAL_STATE");
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

        log.warn("User method argument validation error in {} : {}", request.getDescription(false), errors);

        return new ErrorResponse("User validation failed", "USER_VALIDATION_DATA", errors);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Internal error in {}: {}", request.getDescription(true), ex.getMessage(), ex);

        return new ErrorResponse("Internal server error", "INTERNAL_ERROR", Map.of(ex.getClass().getSimpleName(), ex.getMessage()));
    }

}