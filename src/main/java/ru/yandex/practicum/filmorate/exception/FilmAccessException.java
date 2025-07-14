package ru.yandex.practicum.filmorate.exception;

import java.sql.SQLException;

public class FilmAccessException extends RuntimeException {
    public FilmAccessException(String message) {
        super(message);
    }

    public FilmAccessException(String message, SQLException cause) {
        super(message, cause);
    }

}
