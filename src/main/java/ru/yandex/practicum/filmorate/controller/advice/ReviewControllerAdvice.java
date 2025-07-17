package ru.yandex.practicum.filmorate.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ru.yandex.practicum.filmorate.controller.ReviewController;
import ru.yandex.practicum.filmorate.dto.error.ErrorResponse;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exception.ReviewValidationException;

@Slf4j
@RestControllerAdvice(assignableTypes = ReviewController.class)
public class ReviewControllerAdvice {

    @ExceptionHandler({ReviewNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final ReviewNotFoundException e, WebRequest request) {
        return new ErrorResponse(e.getMessage(), "REVIEW_NOT_FOUND");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ReviewValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleParameterNotValid(final RuntimeException e, WebRequest request) {
        return new ErrorResponse(e.getMessage(), "REVIEW_INVALID_DATA");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnknownError(final Throwable e, WebRequest request) {
        log.info("исключение " + e.getClass().getName());
        return new ErrorResponse(e.getMessage(), "REVIEW_INTERNAL_ERROR");
    }
}
