package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor
public class Review {
    private Integer id;
    @NotBlank(message = "Отзыв не может быть пустым")
    private String content;
    @NotBlank(message = "Тип отзыва не может быть пустым")
    private Boolean isPositive;
    private Integer userId;
    private Integer filmId;
    private Integer useful = 0;
}
