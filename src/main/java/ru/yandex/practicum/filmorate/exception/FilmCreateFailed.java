package ru.yandex.practicum.filmorate.exception;

public class FilmCreateFailed extends RuntimeException {
    public FilmCreateFailed(String message) {
        super(message);
    }
}
