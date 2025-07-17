package ru.yandex.practicum.filmorate.exception;

public class DirectorCreateFailed extends RuntimeException {
    public DirectorCreateFailed(String message) {
        super(message);
    }
}
