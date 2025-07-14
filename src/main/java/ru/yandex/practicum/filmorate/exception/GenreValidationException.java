package ru.yandex.practicum.filmorate.exception;

public class GenreValidationException extends RuntimeException {
    public GenreValidationException(String message) {
        super(message);
    }
}
