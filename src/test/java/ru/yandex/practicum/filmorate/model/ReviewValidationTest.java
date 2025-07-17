package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReviewValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validDirector_shouldPassValidation() {
        final Review review = Review.builder()
                .reviewId(1)
                .content("Интересное кино")
                .isPositive(true)
                .userId(1)
                .filmId(2)
                .useful(0)
                .build();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);

        assertTrue(violations.isEmpty());
    }

    @Test
    public void ifContentIsEmpty() {
        final Review review = Review.builder()
                .reviewId(1)
                .isPositive(true)
                .userId(1)
                .filmId(2)
                .useful(0)
                .build();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Отзыв не может быть пустым")));
    }

    @Test
    public void ifContentIsOnlyWhiteSpase() {
        final Review review = Review.builder()
                .reviewId(1)
                .content(" ")
                .isPositive(true)
                .userId(1)
                .filmId(2)
                .useful(0)
                .build();

        Set<ConstraintViolation<Review>> violations = validator.validate(review);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Отзыв не может быть пустым")));
    }
}
