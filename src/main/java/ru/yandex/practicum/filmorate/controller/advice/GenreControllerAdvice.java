package ru.yandex.practicum.filmorate.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ru.yandex.practicum.filmorate.controller.GenreController;
import ru.yandex.practicum.filmorate.dto.error.ErrorResponse;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;

import java.util.Map;

@Slf4j
@RestControllerAdvice(assignableTypes = GenreController.class)
public class GenreControllerAdvice {

    @ExceptionHandler(GenreNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleGenreNotFound(GenreNotFoundException ex, WebRequest request) {

        log.warn("Genre not found in {}: {}", request.getDescription(false), ex.getMessage());

        return new ErrorResponse(ex.getMessage(), "GENRE_NOT_FOUND");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Internal error in {}: {}", request.getDescription(true), ex.getMessage(), ex);

        return new ErrorResponse("Internal server error", "INTERNAL_ERROR", Map.of(ex.getClass().getSimpleName(), ex.getMessage()));
    }

}