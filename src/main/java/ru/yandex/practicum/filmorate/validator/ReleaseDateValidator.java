package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.annotation.ValidReleaseDate;

import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<ValidReleaseDate, LocalDate> {
    private static final LocalDate CINEMA_BIRTH_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        if (value.isBefore(CINEMA_BIRTH_DATE)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Release date must be after 1895-12-28").addConstraintViolation();
            return false;
        }

        return true;
    }

}
