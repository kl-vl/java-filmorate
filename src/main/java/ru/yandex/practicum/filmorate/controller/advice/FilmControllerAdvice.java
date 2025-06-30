package ru.yandex.practicum.filmorate.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.dto.error.ErrorResponse;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(assignableTypes = FilmController.class)
public class FilmControllerAdvice {

    @ExceptionHandler(FilmNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleFilmNotFound(FilmNotFoundException ex, WebRequest request) {
        log.warn("Film not found in {}: {}", request.getDescription(false), ex.getMessage());

        return new ErrorResponse(ex.getMessage(), "FILM_NOT_FOUND");
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException ex, WebRequest request) {

        log.warn("User not found in {}: {}", request.getDescription(false), ex.getMessage());

        return new ErrorResponse(ex.getMessage(), "USER_NOT_FOUND");
    }

    @ExceptionHandler(MpaNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(MpaNotFoundException ex, WebRequest request) {

        log.warn("Mpa not found in {}: {}", request.getDescription(false), ex.getMessage());

        return new ErrorResponse(ex.getMessage(), "MPA_NOT_FOUND");
    }

    @ExceptionHandler(GenreNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(GenreNotFoundException ex, WebRequest request) {

        log.warn("Genre not found in {}: {}", request.getDescription(false), ex.getMessage());

        return new ErrorResponse(ex.getMessage(), "GENRE_NOT_FOUND");
    }

    @ExceptionHandler({FilmValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidFilm(RuntimeException ex, WebRequest request) {
        log.warn("Film Validation error in {}: {}", request.getDescription(false), ex.getMessage());

        return new ErrorResponse(ex.getMessage(), "FILM_INVALID_DATA");
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

        return new ErrorResponse("Validation failed", "FILM_VALIDATION_ERROR", errors);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Internal error in {}: {}", request.getDescription(true), ex.getMessage(), ex);

        return new ErrorResponse("Internal server error", "INTERNAL_ERROR", Map.of(ex.getClass().getSimpleName(), ex.getMessage()));
    }


}
