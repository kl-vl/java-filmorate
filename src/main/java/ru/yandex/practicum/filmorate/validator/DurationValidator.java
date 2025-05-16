package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.annotation.ValidDuration;

import java.time.Duration;

public class DurationValidator implements ConstraintValidator<ValidDuration, Duration> {
    private long minMinutes;

    @Override
    public void initialize(ValidDuration constraintAnnotation) {
        this.minMinutes = constraintAnnotation.minMinutes();
    }

    @Override
    public boolean isValid(Duration duration, ConstraintValidatorContext context) {
        if (duration == null) {
            return true;
        }
        long totalMinutes = duration.toMinutes();
        return totalMinutes >= minMinutes;
    }
}
