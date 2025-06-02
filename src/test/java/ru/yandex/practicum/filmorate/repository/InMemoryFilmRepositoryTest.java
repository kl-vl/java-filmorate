package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryFilmRepositoryTest {
    private InMemoryFilmRepository filmRepository;
    private InMemoryUserRepository userRepository;
    private Film testFilm;
    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        filmRepository = new InMemoryFilmRepository(userRepository);
        testFilm = new Film(null, "Inception", "A thief who steals corporate secrets.",
                LocalDate.of(2010, 7, 16), Duration.ofMinutes(148)
        );
        testUser = new User(null, "John Doe", "johndoe", "john@example.com", LocalDate.of(1990, 5, 15));
    }

    @Test
    void create_shouldGenerateNewId() {
        final Film created = filmRepository.create(testFilm);

        assertAll("Generate id on create",
                () -> assertNotNull(created.getId()),
                () -> assertEquals(1, created.getId()),
                () -> assertEquals(1, filmRepository.findAll().size())
        );
    }

    @Test
    void create_shouldThrowWhenIdExists() {
        testFilm.setId(1);

        assertThrows(FilmValidationException.class, () -> filmRepository.create(testFilm));
    }

    @Test
    void update_shouldUpdateExistingFilm() {
        final Film created = filmRepository.create(testFilm);
        final Film toUpdate = new Film(created.getId(), "New Name", "New Desc", LocalDate.now(), Duration.ofMinutes(150));

        final Film updated = filmRepository.update(toUpdate);

        assertAll("Update film",
                () -> assertEquals("New Name", updated.getName()),
                () -> assertEquals(1, filmRepository.findAll().size()),
                () -> assertTrue(filmRepository.existsById(1))
        );
    }

    @Test
    void update_shouldThrowWhenFilmNotExists() {
        assertThrows(FilmNotFoundException.class, () ->
                filmRepository.update(new Film(999, "Test", "Desc", LocalDate.now(), Duration.ofMinutes(90))));
    }

    @Test
    void create_shouldCreateWhenNoId() {
        final Film result = filmRepository.create(testFilm);

        assertEquals(1, result.getId());
    }

    @Test
    void update_shouldUpdateWhenHasId() {
        final Film created = filmRepository.create(testFilm);
        final Film toUpdate = new Film(created.getId(), "Updated", "Desc", LocalDate.of(2010, 7, 16), Duration.ofMinutes(148));

        final Film result = filmRepository.update(toUpdate);

        assertEquals("Updated", result.getName());
    }

    @Test
    void addLike_shouldAddLikeWhenFilmAndUserExist() {
        userRepository.create(testUser);
        filmRepository.create(testFilm);

        boolean result = filmRepository.addLike(1, 1);

        assertAll("Film has like 1",
                () -> assertTrue(result),
                () -> assertTrue(filmRepository.hasLike(1, 1))
        );
    }

    @Test
    void addLike_shouldThrowWhenFilmNotExists() {
        userRepository.create(testUser);

        assertThrows(FilmNotFoundException.class, () -> filmRepository.addLike(999, 1));
    }

    @Test
    void addLike_shouldThrowWhenUserNotExists() {
        filmRepository.create(testFilm);

        assertThrows(UserNotFoundException.class, () -> filmRepository.addLike(1, 999));
    }

    @Test
    void removeLike_shouldReturnTrueWhenLikeRemoved() {
        userRepository.create(testUser);
        filmRepository.create(testFilm);
        filmRepository.addLike(1, 1);

        boolean result = filmRepository.removeLike(1, 1);

        assertAll("Remove like",
                () -> assertTrue(result),
                () -> assertFalse(filmRepository.hasLike(1, 1))
        );
    }

    @Test
    void removeLike_shouldReturnFalseWhenLikeNotExists() {
        filmRepository.create(testFilm);

        boolean result = filmRepository.removeLike(1, 999);

        assertFalse(result);
    }

    @Test
    void getPopularFilmIds_shouldReturnOrderedFilmIdsByLikes() {
        for (int i = 1; i <= 3; i++) {
            userRepository.create(testUser);
            testUser.setId(null);
            filmRepository.create(testFilm);
            testFilm.setId(null);
        }

        // Film 1 - 2 likes
        filmRepository.addLike(1, 1);
        filmRepository.addLike(1, 2);
        // Film 2 - 1 like
        filmRepository.addLike(2, 3);
        // Film 3 - no likes

        List<Integer> popular = filmRepository.getPopularFilmIds(2);

        assertEquals(List.of(1, 2), popular);
    }

}