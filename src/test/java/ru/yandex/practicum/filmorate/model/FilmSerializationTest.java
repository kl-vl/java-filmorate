package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class FilmSerializationTest {
    private static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testSerializeDurationToMinutes() throws JsonProcessingException {
        final Film film = new Film(
                1,
                "Inception",
                "A thief who steals corporate secrets",
                LocalDate.of(2010, 7, 16),
                Duration.ofMinutes(148)
        );

        final String json = mapper.writeValueAsString(film);

        assertAll("Duration to minutes serialization",
                () -> assertTrue(json.contains("\"duration\":148")),
                () -> assertTrue(json.contains("\"releaseDate\":\"2010-07-16\""))
        );
    }

    @Test
    void testDeserializeMinutesToDuration() throws JsonProcessingException {
        final String json = """
                {
                    "id": 1,
                    "name": "Inception",
                    "description": "Description",
                    "releaseDate": "2010-07-16",
                    "duration": 120
                }
                """;

        final Film film = mapper.readValue(json, Film.class);

        assertAll("Duration from minutes deserialization",
                () -> assertEquals(Duration.ofMinutes(120), film.getDuration()),
                () -> assertEquals(LocalDate.of(2010, 7, 16), film.getReleaseDate())
        );
    }

    @Test
    void testDeserializeWithoutReleaseDate_shouldSetNull() throws JsonProcessingException {
        final String json = """
                {
                    "id": 1,
                    "name": "Inception",
                    "description": "Description",
                    "duration": 120
                }
                """;

        final Film film = mapper.readValue(json, Film.class);

        assertNull(film.getReleaseDate());
    }

    @Test
    void testSerializeWithNullReleaseDate() throws JsonProcessingException {
        final Film film = new Film(
                1,
                "Inception",
                "A thief who steals corporate secrets",
                null,
                Duration.ofMinutes(148)
        );

        final String json = mapper.writeValueAsString(film);

        assertTrue(json.contains("\"releaseDate\":null"));
    }

    @Test
    void testDeserializeInvalidDateFormat_shouldThrowException() {
        final String json = """
                {
                    "id": 1,
                    "name": "Inception",
                    "description": "Description",
                    "releaseDate": "16-07-2010", // Неправильный формат
                    "duration": 120
                }
                """;

        assertThrows(JsonProcessingException.class, () -> {
            mapper.readValue(json, Film.class);
        });
    }
}