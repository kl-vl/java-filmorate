package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validUser_shouldPassValidation() {
        final User user = new User(1, "John Doe", "johndoe", "john@example.com", LocalDate.now().minusYears(20));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }

    @Test
    void negativeId_shouldFailValidation() {
        final User user = new User(-1, "John", "johndoe", "john@example.com", LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertAll("Negative id",
                () -> assertEquals(1, violations.size()),
                () -> assertEquals("User ID must be positive number", violations.iterator().next().getMessage())
        );
    }

    @Test
    void blankLogin_shouldFailValidation() {
        final User user = new User(1, "John", " ", "john@example.com", LocalDate.now());

        final List<String> expectedMessages = List.of("must not be blank", "ust not contain whitespace");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertAll("Blank login",
                () -> assertEquals(2, violations.size()),
                () -> assertTrue(expectedMessages.stream()
                        .allMatch(expected -> violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .anyMatch(actual -> actual.contains(expected))))
        );
    }

    @Test
    void nullLogin_shouldFailValidation() {
        final User user = new User(1, "John", null, "john@example.com", LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertAll("Null login",
                () -> assertEquals(2, violations.size()),
                () -> assertTrue(violations.stream()
                        .anyMatch(v -> "{jakarta.validation.constraints.NotNull.message}".equals(v.getMessageTemplate())))
        );
    }

    @Test
    void invalidEmail_shouldFailValidation() {
        final User user = new User(1, "John", "johndoe", "invalid-email", LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertAll("Invalid email",
                () -> assertEquals(1, violations.size()),
                () -> assertEquals("Not valid email format", violations.iterator().next().getMessage())
        );
    }

    @Test
    void futureBirthday_shouldFailValidation() {
        final User user = new User(1, "John", "johndoe", "john@example.com", LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertAll("Future birthday",
                () -> assertEquals(1, violations.size()),
                () -> assertEquals("{jakarta.validation.constraints.PastOrPresent.message}",
                        violations.iterator().next().getMessageTemplate())
        );
    }

    @Test
    void nullBirthday_shouldFailValidation() {
        final User user = new User(1, "John", "johndoe", "john@example.com", null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertAll("Null birthday",
                () -> assertEquals(1, violations.size()),
                () -> assertEquals("{jakarta.validation.constraints.NotNull.message}",
                        violations.iterator().next().getMessageTemplate())
        );
    }

    @Test
    void minimumValidId_shouldPassValidation() {
        final User user = new User(1, null, "login", "email@example.com", LocalDate.now());

        assertDoesNotThrow(() -> validator.validate(user));
    }

    @Test
    void emptyEmail_shouldBeAllowed() {
        final User user = new User(1, "John", "johndoe", "", LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }

    @Test
    void nullEmail_shouldBeAllowed() {
        final User user = new User(1, "John", "johndoe", null, LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }

    @Test
    void todayBirthday_shouldPassValidation() {
        final User user = new User(1, "John", "johndoe", "john@example.com", LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }

    @Test
    void fullValidation_shouldCatchMultipleErrors() {
        final User user = new User(-1, " ", null, "invalid", LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        Set<String> messageTemplates = violations.stream()
                .map(ConstraintViolation::getMessageTemplate)
                .collect(Collectors.toSet());

        Set<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertAll("All validation errors",
                () -> assertEquals(5, violations.size(),
                        "Should detect 5 validation errors"),

                () -> assertTrue(messages.contains("User ID must be positive number"),
                        "Missing positive ID validation"),
                () -> assertTrue(messages.contains("Login must not be blank"),
                        "Missing blank login validation"),
                () -> assertTrue(messages.contains("Not valid email format"),
                        "Missing email format validation"),

                () -> assertTrue(messageTemplates.contains("{jakarta.validation.constraints.NotNull.message}"),
                        "Missing not null validation"),
                () -> assertTrue(messageTemplates.contains("{jakarta.validation.constraints.PastOrPresent.message}"),
                        "Missing date validation")
        );
    }
}
