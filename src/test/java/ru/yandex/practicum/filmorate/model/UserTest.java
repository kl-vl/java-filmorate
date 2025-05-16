package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {

    @Test
    void setName_shouldUseLoginWhenNameIsBlank() {
        final User user = new User(1, "", "johndoe", "john@example.com", LocalDate.now());

        assertEquals("johndoe", user.getName());
    }

    @Test
    void setName_shouldUseLoginWhenNameIsNull() {
        final User user = new User(1, null, "johndoe", "john@example.com", LocalDate.now());

        assertEquals("johndoe", user.getName());
    }

    @Test
    void setName_shouldUseLoginWhenNameIsWhitespace() {
        final User user = new User(1, "   ", "johndoe", "john@example.com", LocalDate.now());

        assertEquals("johndoe", user.getName());
    }

    @Test
    void setName_shouldKeepNameWhenNotEmpty() {
        final User user = new User(1, "John Doe", "johndoe", "john@example.com", LocalDate.now());

        assertEquals("John Doe", user.getName());
    }

}