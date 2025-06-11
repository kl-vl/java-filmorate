package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.annotation.ValidDuration;
import ru.yandex.practicum.filmorate.annotation.ValidReleaseDate;
import ru.yandex.practicum.filmorate.converter.DurationMinutesConverter;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Film {

    @Positive(message = "Film ID must be positive number")
    private Integer id;

    @NotBlank
    private String name;

    @NotBlank
    @Length(max = 200, message = "Film Description length must be less than 200 characters")
    private String description;

    @ValidReleaseDate
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @NotNull
    @ValidDuration(message = "Duration must be positive")
    @JsonSerialize(using = DurationMinutesConverter.Serializer.class)
    @JsonDeserialize(using = DurationMinutesConverter.Deserializer.class)
    private Duration duration;

    @ToString.Include(name = "duration")
    private long getDurationMinutes() {
        return duration.toMinutes();
    }

    @NotNull
    private Raiting rating;

    private final Set<Genre> genres = new HashSet<>();

}
