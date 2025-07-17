package ru.yandex.practicum.filmorate.enums;

import java.util.Optional;

public enum DirectorSortBy {
    YEAR,
    LIKES;

    public static Optional<DirectorSortBy> fromString(String value) {
        try {
            return Optional.of(valueOf(value.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
