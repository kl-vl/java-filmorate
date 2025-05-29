package ru.yandex.practicum.filmorate.dto.error;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private final String message;
    private final String errorType;
    private final LocalDateTime timestamp;
    private final Map<String, String> details;

    public ErrorResponse(String message, String errorType) {
        this(message, errorType, LocalDateTime.now(), null);
    }

    public ErrorResponse(String message, String errorType, Map<String, String> details) {
        this(message, errorType, LocalDateTime.now(), details);
    }

}