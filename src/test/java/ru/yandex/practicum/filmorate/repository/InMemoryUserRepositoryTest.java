package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
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

    private InMemoryUserRepository userRepository;
    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        testUser = new User(null, "John Doe", "johndoe", "john@example.com", LocalDate.of(1990, 5, 15));
    }

    @Test
    void create_shouldGenerateIdAndSaveUser() {
        final User createdUser = userRepository.create(testUser).orElseThrow(() -> new UserNotFoundException("User not found"));

        assertAll("Generate id on User create",
                () -> assertNotNull(createdUser.getId()),
                () -> assertEquals(1, createdUser.getId()),
                () -> assertEquals(1, userRepository.findAll().size())
        );
    }

    @Test
    void create_shouldThrowWhenUserHasId() {
        testUser.setId(1);

        assertThrows(UserValidationException.class, () -> userRepository.create(testUser));
    }

    @Test
    void create_shouldIncrementIdCounter() {
        final User user1 = userRepository.create(testUser).orElseThrow(() -> new UserNotFoundException("User not found"));
        final User user2 = userRepository.create(new User(null, "Jane", "jane", "jane@example.com", LocalDate.now())).orElseThrow(() -> new UserNotFoundException("User not found"));;

        assertAll("Increment id counter",
                () -> assertEquals(1, user1.getId()),
                () -> assertEquals(2, user2.getId())
        );
    }

    @Test
    void update_shouldUpdateExistingUser() {
        final User createdUser = userRepository.create(testUser).orElseThrow(() -> new UserNotFoundException("User not found"));
        final User updatedUser = new User(createdUser.getId(), "John Updated", "johndoe", "new@example.com", LocalDate.now());

        final User result = userRepository.update(updatedUser).orElseThrow(() -> new UserNotFoundException("User not found"));

        assertAll("Update user",
                () -> assertEquals("John Updated", result.getName()),
                () -> assertEquals("new@example.com", result.getEmail()),
                () -> assertEquals(1, userRepository.findAll().size())
        );
    }

    @Test
    void update_shouldThrowWhenUserNotFound() {
        final User nonExistingUser = new User(999, "Unknown", "unknown", "email@example.com", LocalDate.now());

        assertThrows(UserNotFoundException.class, () -> userRepository.update(nonExistingUser));
    }

    @Test
    void update_shouldThrowWhenUserIdIsNull() {
        final User userWithoutId = new User(null, "No ID", "noid", "email@example.com", LocalDate.now());

        assertThrows(UserValidationException.class, () -> userRepository.update(userWithoutId));
    }

    @Test
    void findAll_shouldReturnEmptyListForNewRepository() {
        final List<User> users = userRepository.findAll();

        assertTrue(users.isEmpty());
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        final User user1 = userRepository.create(testUser).orElseThrow(() -> new UserNotFoundException("User not found"));
        final User user2 = userRepository.create(new User(null, "Jane", "jane", "jane@example.com", LocalDate.now())).orElseThrow(() -> new UserNotFoundException("User not found"));

        final List<User> users = userRepository.findAll();

        assertAll("Find all users",
                () -> assertEquals(2, users.size()),
                () -> assertTrue(users.contains(user1)),
                () -> assertTrue(users.contains(user2))
        );
    }

    @Test
    void existsById_shouldReturnFalseForNonExistingId() {
        assertFalse(userRepository.existsById(999));
    }

    @Test
    void existsById_shouldReturnTrueForExistingId() {
        final User createdUser = userRepository.create(testUser).orElseThrow(() -> new UserNotFoundException("User not found"));

        assertTrue(userRepository.existsById(createdUser.getId()));
    }

    @Test
    void addFriend_shouldAddFriend() {
        userRepository.create(testUser);
        testUser.setId(null);
        userRepository.create(testUser);

        boolean result = userRepository.addFriend(1, 2);

        assertAll("Add friend",
                () -> assertTrue(result),
                () -> assertTrue(userRepository.areFriends(1, 2))
        );
    }

    @Test
    void removeFriend_shouldRemoveFriend() {
        userRepository.create(testUser);
        testUser.setId(null);
        userRepository.create(testUser);
        userRepository.addFriend(1, 2);

        boolean result = userRepository.removeFriend(1, 2);

        assertAll("Remove friends",
                () -> assertTrue(result),
                () -> assertFalse(userRepository.areFriends(1, 2))
        );
    }

    @Test
    void removeFriend_shouldReturnFalseWhenNoFriendship() {
        userRepository.create(testUser);

        boolean result = userRepository.removeFriend(1, 999);

        assertFalse(result);
    }

    @Test
    void getFriends_shouldReturnFriendsList() {
        for (int i = 1; i <= 3; i++) {
            userRepository.create(testUser);
            testUser.setId(null);
        }
        userRepository.addFriend(1, 2);
        userRepository.addFriend(1, 3);

        List<Integer> friends = userRepository.getFriends(1);

        assertAll("Get friends",
                () -> assertEquals(2, friends.size()),
                () -> assertTrue(friends.containsAll(List.of(2, 3)))
        );
    }

    @Test
    void getCommonFriends_shouldReturnCommonFriends() {
        for (int i = 1; i <= 3; i++) {
            userRepository.create(testUser);
            testUser.setId(null);
        }
        userRepository.addFriend(1, 3);
        userRepository.addFriend(2, 3);

        List<Integer> commonFriends = userRepository.getCommonFriends(1, 2);

        assertAll("Get common friends",
                () -> assertEquals(1, commonFriends.size()),
                () -> assertEquals(3, commonFriends.get(0))
        );
    }
}