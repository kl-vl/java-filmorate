package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.enums.EventOperation;
import ru.yandex.practicum.filmorate.enums.EventType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private Integer eventId;
    private EventType eventType;
    private EventOperation operation;
    private Integer userId;
    private Integer entityId;
    private Long timestamp;

}
