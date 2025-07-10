package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validFilm_shouldPassValidation() {
        Film film = new Film(
                1, "Inception", "A thief who steals corporate secrets",
                LocalDate.of(2010, 7, 16), Duration.ofMinutes(148)
        );

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
    }

    @Test
    void negativeId_shouldFailValidation() {
        Film film = new Film(-1, "Inception", "Description", LocalDate.now(), Duration.ofMinutes(120));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertAll("Negative id",
                () -> assertEquals(1, violations.size()),
                () -> assertEquals("Film ID must be positive number", violations.iterator().next().getMessage())
        );
    }

    @Test
    void blankName_shouldFailValidation() {
        Film film = new Film(1, "", "Description", LocalDate.now(), Duration.ofMinutes(120));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertAll("Blank name",
                () -> assertEquals(1, violations.size()),
                () -> assertEquals("{jakarta.validation.constraints.NotBlank.message}",
                        violations.iterator().next().getMessageTemplate())
        );
    }

    @Test
    void longDescription_shouldFailValidation() {
        String longDesc = "a".repeat(201);
        Film film = new Film(1, "Inception", longDesc, LocalDate.now(), Duration.ofMinutes(120));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertAll("Long description",
                () -> assertEquals(1, violations.size()),
                () -> assertEquals("Film Description length must be less than 200 characters",
                        violations.iterator().next().getMessage())
        );
    }

    @Test
    void nullDuration_shouldFailValidation() {
        Film film = new Film(1, "Inception", "A thief who steals corporate secrets", LocalDate.now(), null);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertAll("Null duration",
                () -> assertEquals(1, violations.size()),
                () -> assertEquals("{jakarta.validation.constraints.NotNull.message}",
                        violations.iterator().next().getMessageTemplate())
        );
    }

    @Test
    void futureReleaseDate_shouldFailValidation() {
        Film film = new Film(1, "Future Film", "Description",
                LocalDate.now().plusYears(1), Duration.ofMinutes(120));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertAll("Future release date",
                () -> assertEquals(1, violations.size()),
                () -> assertEquals("Release date cannot be in the future", violations.iterator().next().getMessage())
        );
    }

    @Test
    void earlyCinemaRelease_shouldFailValidation() {
        Film film = new Film(1, "Too Old", "Description", LocalDate.of(1890, 1, 1), Duration.ofMinutes(1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertAll("Early Cinema release date",
                () -> assertEquals(1, violations.size()),
                () -> assertEquals("Release date must be after 1895-12-28", violations.iterator().next().getMessage())
        );
    }
}

