package ru.yandex.practicum.filmorate.exception;

public class ReviewValidationException extends RuntimeException {
    public ReviewValidationException(String message) {
        super(message);
    }
}
