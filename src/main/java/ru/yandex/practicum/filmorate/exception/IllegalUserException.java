package ru.yandex.practicum.filmorate.exception;

public class IllegalUserException extends RuntimeException {
    public IllegalUserException(String message) {
        super(message);
    }
}
