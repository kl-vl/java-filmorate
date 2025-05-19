package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.IllegalFilmException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryFilmRepositoryTest {
    private InMemoryFilmRepository repository;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFilmRepository();
        testFilm = new Film(null, "Inception", "A thief who steals corporate secrets.",
                LocalDate.of(2010, 7, 16), Duration.ofMinutes(148)
        );
    }

    @Test
    void create_shouldGenerateNewId() {
        final Film created = repository.create(testFilm);

        assertAll("Generate id on create",
                () -> assertNotNull(created.getId()),
                () -> assertEquals(1, created.getId()),
                () -> assertEquals(1, repository.findAll().size())
        );
    }

    @Test
    void create_shouldThrowWhenIdExists() {
        testFilm.setId(1);

        assertThrows(IllegalFilmException.class, () -> repository.create(testFilm));
    }

    @Test
    void update_shouldUpdateExistingFilm() {
        final Film created = repository.create(testFilm);
        final Film toUpdate = new Film(created.getId(), "New Name", "New Desc", LocalDate.now(), Duration.ofMinutes(150));

        final Film updated = repository.update(toUpdate);

        assertAll("Update film",
                () -> assertEquals("New Name", updated.getName()),
                () -> assertEquals(1, repository.findAll().size()),
                () -> assertTrue(repository.existsById(1))
        );
    }

    @Test
    void update_shouldThrowWhenFilmNotExists() {
        assertThrows(FilmNotFoundException.class, () ->
                repository.update(new Film(999, "Test", "Desc", LocalDate.now(), Duration.ofMinutes(90))));
    }

    @Test
    void create_shouldCreateWhenNoId() {
        final Film result = repository.create(testFilm);

        assertEquals(1, result.getId());
    }

    @Test
    void update_shouldUpdateWhenHasId() {
        final Film created = repository.create(testFilm);
        final Film toUpdate = new Film(created.getId(), "Updated", "Desc", LocalDate.of(2010, 7, 16), Duration.ofMinutes(148));

        final Film result = repository.update(toUpdate);

        assertEquals("Updated", result.getName());
    }

}