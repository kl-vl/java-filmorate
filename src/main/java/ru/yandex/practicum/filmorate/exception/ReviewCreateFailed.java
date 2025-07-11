package ru.yandex.practicum.filmorate.exception;

public class ReviewCreateFailed extends RuntimeException {
    public ReviewCreateFailed(String message) {
        super(message);
    }
}
