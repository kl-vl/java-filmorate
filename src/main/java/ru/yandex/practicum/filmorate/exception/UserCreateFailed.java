package ru.yandex.practicum.filmorate.exception;

public class UserCreateFailed extends RuntimeException {
    public UserCreateFailed(String message) {
        super(message);
    }
}
