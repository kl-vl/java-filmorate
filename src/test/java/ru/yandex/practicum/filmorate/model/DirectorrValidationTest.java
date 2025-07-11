package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectorrValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validDirector_shouldPassValidation() {
        final Director director = new Director(1, "John Doe");

        Set<ConstraintViolation<Director>> violations = validator.validate(director);

        assertTrue(violations.isEmpty());
    }

    @Test
    void blankName_shouldFailValidation() {
        final Director director = new Director(1, " ");

        final List<String> expectedMessages = List.of("must not be blank", "ust not contain whitespace");

        Set<ConstraintViolation<Director>> violations = validator.validate(director);

        System.out.println(violations);

        assertAll("Blank name",
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream()
                        .anyMatch(v -> v.getMessage().contains("must not be blank")))
        );
    }

    @Test
    void nullName_shouldFailValidation() {
        final Director director = new Director(1,  null);

        Set<ConstraintViolation<Director>> violations = validator.validate(director);

        assertAll("Null name",
                () -> assertEquals(2, violations.size()),
                () -> assertTrue(violations.stream()
                        .anyMatch(v -> "{jakarta.validation.constraints.NotNull.message}".equals(v.getMessageTemplate())))
        );
    }

}
