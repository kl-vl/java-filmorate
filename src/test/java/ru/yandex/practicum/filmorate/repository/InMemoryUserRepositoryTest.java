package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.IllegalUserException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repository;
    private User testUser;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        testUser = new User(null, "John Doe", "johndoe", "john@example.com", LocalDate.of(1990, 5, 15));
    }

    @Test
    void create_shouldGenerateIdAndSaveUser() {
        final User createdUser = repository.create(testUser);

        assertAll("Generate id on User create",
                () -> assertNotNull(createdUser.getId()),
                () -> assertEquals(1, createdUser.getId()),
                () -> assertEquals(1, repository.findAll().size())
        );
    }

    @Test
    void create_shouldThrowWhenUserHasId() {
        testUser.setId(1);

        assertThrows(IllegalUserException.class, () -> repository.create(testUser));
    }

    @Test
    void create_shouldIncrementIdCounter() {
        final User user1 = repository.create(testUser);
        final User user2 = repository.create(new User(null, "Jane", "jane", "jane@example.com", LocalDate.now()));

        assertAll("Increment id counter",
                () -> assertEquals(1, user1.getId()),
                () -> assertEquals(2, user2.getId())
        );
    }

    @Test
    void update_shouldUpdateExistingUser() {
        final User createdUser = repository.create(testUser);
        final User updatedUser = new User(createdUser.getId(), "John Updated", "johndoe", "new@example.com", LocalDate.now());

        final User result = repository.update(updatedUser);

        assertAll("Update user",
                () -> assertEquals("John Updated", result.getName()),
                () -> assertEquals("new@example.com", result.getEmail()),
                () -> assertEquals(1, repository.findAll().size())
        );
    }

    @Test
    void update_shouldThrowWhenUserNotFound() {
        final User nonExistingUser = new User(999, "Unknown", "unknown", "email@example.com", LocalDate.now());

        assertThrows(UserNotFoundException.class, () -> repository.update(nonExistingUser));
    }

    @Test
    void update_shouldThrowWhenUserIdIsNull() {
        final User userWithoutId = new User(null, "No ID", "noid", "email@example.com", LocalDate.now());

        assertThrows(IllegalUserException.class, () -> repository.update(userWithoutId));
    }

    @Test
    void findAll_shouldReturnEmptyListForNewRepository() {
        final List<User> users = repository.findAll();

        assertTrue(users.isEmpty());
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        final User user1 = repository.create(testUser);
        final User user2 = repository.create(new User(null, "Jane", "jane", "jane@example.com", LocalDate.now()));

        final List<User> users = repository.findAll();

        assertAll("Find all users",
                () -> assertEquals(2, users.size()),
                () -> assertTrue(users.contains(user1)),
                () -> assertTrue(users.contains(user2))
        );
    }

    @Test
    void existsById_shouldReturnFalseForNonExistingId() {
        assertFalse(repository.existsById(999));
    }

    @Test
    void existsById_shouldReturnTrueForExistingId() {
        final User createdUser = repository.create(testUser);

        assertTrue(repository.existsById(createdUser.getId()));
    }

    /*@Test
    void update_shouldNotAffectOtherUsers() {
        final User user1 = repository.create(testUser);
        final User user2 = repository.create(new User(null, "Jane", "jane", "jane@example.com", LocalDate.now()));
        final User updatedUser = new User(user1.getId(), "Updated Name", user1.getLogin(), user1.getEmail(), user1.getBirthday());

        repository.update(updatedUser);

        List<User> users = repository.findAll();

        assertAll("Update user",
                () -> assertEquals(2, users.size()),
                () -> assertEquals("Updated Name", users.get(0).getName()),
                () -> assertEquals("Jane", users.get(1).getName())
        );
    }*/
}