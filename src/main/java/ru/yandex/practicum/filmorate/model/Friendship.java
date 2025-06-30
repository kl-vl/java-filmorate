package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Friendship {
    private Integer userId;
    private Integer friendId;
    private Boolean accepted = true;

    public Friendship(Integer userId, Integer friendId) {
        this(userId, friendId, true);
    }
}
