package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void getDurationMinutes_shouldReturnCorrectValue() {
        Film film = new Film(
                1,
                "Inception",
                "Description",
                null,
                Duration.ofHours(2).plusMinutes(28) // 148 minutes
        );

        assertEquals(148, film.getDuration().toMinutes());
    }

    @Test
    void toString_shouldContainDurationInMinutes() {
        Film film = new Film(
                1,
                "Inception",
                "A thief who steals corporate secrets",
                LocalDate.of(2010, 7, 16),
                Duration.ofMinutes(148)
        );

        String toString = film.toString();

        assertAll("Film duration",
                () -> assertTrue(toString.contains("duration=148")),
                () -> assertTrue(toString.contains("name=Inception"))
        );
    }

    @Test
    void equalsAndHashCode_shouldWorkCorrectly() {
        Film film1 = new Film(1, "Film", "Desc", null, Duration.ofMinutes(90));
        Film film2 = new Film(1, "Film", "Desc", null, Duration.ofMinutes(90));

        assertAll("Film hashcode",
                () -> assertEquals(film1, film2),
                () -> assertEquals(film1.hashCode(), film2.hashCode())
        );
    }

    @Test
    void minimumValidDuration_shouldPassValidation() {
        Film film = new Film(
                1,
                "Short Film",
                "Description",
                LocalDate.now(),
                Duration.ofMinutes(1)
        );

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
    }

    @Test
    void zeroDuration_shouldFailValidation() {
        Film film = new Film(
                1,
                "Zero Film",
                "Description",
                LocalDate.now(),
                Duration.ZERO
        );

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertAll("Zero duration",
                () -> assertEquals(1, violations.size()),
                () -> assertEquals("Duration must be positive", violations.iterator().next().getMessage())
        );
    }

    @Test
    void longFilmName_shouldNotPassValidation() {
        final String descriptionOver200 = "d".repeat(201);
        final Film film = new Film(1, "Film name", descriptionOver200, LocalDate.now(), Duration.ofMinutes(1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertAll("Long description",
                () -> assertEquals(1, violations.size()),
                () -> assertEquals("Film Description length must be less than 200 characters",
                        violations.iterator().next().getMessage())
        );

    }

}