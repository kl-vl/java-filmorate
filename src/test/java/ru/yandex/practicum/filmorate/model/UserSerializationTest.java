package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserSerializationTest {
    private static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void serializeUser_shouldProduceValidJson() throws JsonProcessingException {
        final User user = new User(1, "John Doe", "johndoe", "john@example.com", LocalDate.of(1990, 5, 15));

        final String json = mapper.writeValueAsString(user);

        assertAll("User serialization",
                () -> assertTrue(json.contains("\"id\":1")),
                () -> assertTrue(json.contains("\"name\":\"John Doe\"")),
                () -> assertTrue(json.contains("\"login\":\"johndoe\"")),
                () -> assertTrue(json.contains("\"email\":\"john@example.com\"")),
                () -> assertTrue(json.contains("\"birthday\":\"1990-05-15\""))
        );
    }

    @Test
    void deserializeJson_shouldCreateValidUser() throws JsonProcessingException {
        final String json = """
                {
                    "id": 1,
                    "name": "John Doe",
                    "login": "johndoe",
                    "email": "john@example.com",
                    "birthday": "1990-05-15"
                }
                """;

        final User user = mapper.readValue(json, User.class);

        assertAll("User deserialization",
                () -> assertEquals(1, user.getId()),
                () -> assertEquals("John Doe", user.getName()),
                () -> assertEquals("johndoe", user.getLogin()),
                () -> assertEquals("john@example.com", user.getEmail()),
                () -> assertEquals(LocalDate.of(1990, 5, 15), user.getBirthday())
        );
    }

    @Test
    void deserializeJsonWithoutName_shouldUseLogin() throws JsonProcessingException {
        final String json = """
                {
                    "id": 1,
                    "login": "johndoe",
                    "email": "john@example.com",
                    "birthday": "1990-05-15"
                }
                """;

        final User user = mapper.readValue(json, User.class);

        assertEquals("johndoe", user.getName()); // Проверка автоматической подстановки login
    }

    @Test
    void deserializeMinimalUser_shouldWork() throws JsonProcessingException {
        final String json = """
                {
                    "login": "johndoe",
                    "birthday": "1990-05-15"
                }
                """;

        final User user = mapper.readValue(json, User.class);

        assertAll("User deserialization",
                () -> assertEquals("johndoe", user.getLogin()),
                () -> assertEquals("johndoe", user.getName()),
                () -> assertNull(user.getEmail())
        );
    }

    @Test
    void deserializeJsonWithNullEmail_shouldWork() throws JsonProcessingException {
        final String json = """
                {
                    "id": 1,
                    "name": "John Doe",
                    "login": "johndoe",
                    "email": null,
                    "birthday": "1990-05-15"
                }
                """;

        final User user = mapper.readValue(json, User.class);

        assertNull(user.getEmail());
    }

}
