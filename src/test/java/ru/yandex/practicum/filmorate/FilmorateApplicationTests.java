package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.DbFilmRepository;
import ru.yandex.practicum.filmorate.repository.DbGenreRepository;
import ru.yandex.practicum.filmorate.repository.DbMpaRepository;
import ru.yandex.practicum.filmorate.repository.DbUserRepository;
import ru.yandex.practicum.filmorate.repository.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.UserRowMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DbUserRepository.class, DbFilmRepository.class, DbGenreRepository.class, DbMpaRepository.class, FilmRowMapper.class, UserRowMapper.class, GenreRowMapper.class, MpaRowMapper.class})
@Transactional
class FilmorateApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DbUserRepository userRepository;

    @Autowired
    private DbFilmRepository filmRepository;

    @Autowired
    private DbMpaRepository mpaRepository;

    @Autowired
    private DbGenreRepository genreRepository;

    private User testUser1;
    private User testUser2;
    private Film testFilm1;
    private Film testFilm2;

    @BeforeEach
    void setUp() {
        testUser1 = User.builder()
                .email("test1@mail.com")
                .login("testLogin1")
                .name("Test Name 1")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        testUser2 = User.builder()
                .email("test2@mail.com")
                .login("testLogin2")
                .name("Test Name 2")
                .birthday(LocalDate.of(2002, 2, 2))
                .build();

        testFilm1 =  Film.builder()
                    .name("Test Film 1")
                    .description("Test Description 1")
                    .releaseDate(LocalDate.of(2001, 1, 1))
                    .duration(Duration.ofMinutes(121))
                    .mpa(mpaRepository.getMpaById(1).orElseThrow())
                    .genres(Set.of(
                            new Genre(1, "Комедия"),
                            new Genre(2, "Драма")
                    ))
                    .build();

        testFilm2 =  Film.builder()
                .name("Test Film 2")
                .description("Test Description 2")
                .releaseDate(LocalDate.of(2002, 2, 2))
                .duration(Duration.ofMinutes(122))
                .mpa(mpaRepository.getMpaById(2).orElseThrow())
                .genres(new LinkedHashSet<>())
                .build();

        jdbcTemplate.execute("DELETE FROM \"film_genre\"");
        jdbcTemplate.execute("DELETE FROM \"film_like\"");
        jdbcTemplate.execute("DELETE FROM \"friendship\"");
        jdbcTemplate.execute("DELETE FROM \"user\"");
        jdbcTemplate.execute("DELETE FROM \"film\"");
    }

    @Test
    void createUser_ShouldReturnUserWithId() {
        Optional<User> result = userRepository.create(testUser1);

        assertThat(result)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getId()).isNotNull();
                    assertThat(user.getEmail()).isEqualTo(testUser1.getEmail());
                });
    }

    @Test
    void findUserById_ShouldReturnCorrectUser() {
        User createdUser = userRepository.create(testUser1).orElseThrow();

        Optional<User> foundUser = userRepository.getById(createdUser.getId());

        assertThat(foundUser)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user.getEmail()).isEqualTo(testUser1.getEmail())
                );
    }

    @Test
    void updateUser_ShouldModifyExistingUser() {
        User created = userRepository.create(testUser1).orElseThrow();
        created.setName("Updated Name");

        Optional<User> updated = userRepository.update(created);

        assertThat(updated)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user.getName()).isEqualTo("Updated Name")
                );
    }

    @Test
    void addFriend_ShouldCreateFriendship() {
        User user1 = userRepository.create(testUser1).orElseThrow();
        User user2 = userRepository.create(testUser2).orElseThrow();

        assertTrue(userRepository.addFriend(user1.getId(), user2.getId()));
        assertTrue(userRepository.areFriends(user1.getId(), user2.getId()));
    }

    @Test
    void removeFriend_ShouldDeleteFriendship() {
        User user1 = userRepository.create(testUser1).orElseThrow();
        User user2 = userRepository.create(testUser2).orElseThrow();

        userRepository.addFriend(user1.getId(), user2.getId());

        assertTrue(userRepository.removeFriend(user1.getId(), user2.getId()));
        assertFalse(userRepository.areFriends(user1.getId(), user2.getId()));
    }

    @Test
    void getCommonFriends_ShouldReturnMutualFriends() {
        User user1 = userRepository.create(testUser1).orElseThrow();
        User user2 = userRepository.create(testUser2).orElseThrow();
        User commonFriend = userRepository.create(
                testUser1.toBuilder().email("common@test.com").login("common").build()
        ).orElseThrow();

        userRepository.addFriend(user1.getId(), commonFriend.getId());
        userRepository.addFriend(user2.getId(), commonFriend.getId());

        List<Integer> commonFriends = userRepository.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(commonFriend.getId(), commonFriends.getFirst());
    }

    @Test
    void createFilm_ShouldReturnFilmWithId() {

        Film createdFilm = filmRepository.create(testFilm1).orElseThrow();

        assertNotNull(createdFilm.getId());
        assertEquals(testFilm1.getName(), createdFilm.getName());
        assertEquals(2, createdFilm.getGenres().size());
    }

    @Test
    void updateFilm_ShouldModifyExistingFilm() {
        Film original = filmRepository.create(testFilm1).orElseThrow();
        original.setName("Updated Name");
        original.setDuration(Duration.ofMinutes(150));

        Film updated = filmRepository.update(original).orElseThrow();

        assertEquals("Updated Name", updated.getName());
        assertEquals(150, updated.getDuration().toMinutes());
    }

    @Test
    void getById_ShouldReturnFilmWithDetails() {
        Film testFilm = filmRepository.create(testFilm1).orElseThrow();

        Optional<Film> foundFilm = filmRepository.getById(testFilm.getId());

        assertTrue(foundFilm.isPresent());
        assertEquals(testFilm.getId(), foundFilm.get().getId());
        assertEquals(2, foundFilm.get().getGenres().size());
    }

    @Test
    void findAll_ShouldReturnAllFilms() {
        filmRepository.create(testFilm1).orElseThrow();
        filmRepository.create(testFilm2).orElseThrow();

        List<Film> films = filmRepository.findAll();

        assertEquals(2, films.size());
        assertTrue(films.stream().anyMatch(f -> f.getName().equals("Test Film 1")));
        assertTrue(films.stream().anyMatch(f -> f.getName().equals("Test Film 2")));
    }

    @Test
    void getPopularFilms_ShouldReturnOrderedByLikes() {
        Film film1 = filmRepository.create(testFilm1).orElseThrow();
        Film film2 = filmRepository.create(testFilm2).orElseThrow();
        User user1 = userRepository.create(testUser1).orElseThrow();
        User user2 = userRepository.create(testUser2).orElseThrow();


        filmRepository.addLike(film1.getId(), user1.getId());
        filmRepository.addLike(film1.getId(), user2.getId());
        filmRepository.addLike(film2.getId(), user1.getId());

        List<Film> popularFilms = filmRepository.getPopularFilms(1);

        assertEquals(1, popularFilms.size());
        assertEquals(film1.getId(), popularFilms.getFirst().getId());
    }

    @Test
    void addAndRemoveLike_ShouldWorkCorrectly() {
        Film film = filmRepository.create(testFilm1).orElseThrow();
        User user1 = userRepository.create(testUser1).orElseThrow();


        assertTrue(filmRepository.addLike(film.getId(), user1.getId()));
        assertFalse(filmRepository.addLike(film.getId(), user1.getId())); // Дубликат

        assertTrue(filmRepository.removeLike(film.getId(), user1.getId()));
        assertFalse(filmRepository.removeLike(film.getId(), user1.getId())); // Уже удален
    }

   @Test
    void validateMpa_ShouldThrowWhenInvalid() {
        testFilm1.setMpa(new Mpa(999, "Invalid"));

        assertThrows(MpaNotFoundException.class, () -> filmRepository.create(testFilm1).orElseThrow());
    }

    @Test
    void validateGenres_ShouldThrowWhenInvalid() {
        testFilm2.addGenre(new Genre(999, "Invalid Genre"));

        assertThrows(GenreNotFoundException.class,
                () -> filmRepository.create(testFilm2));
    }

    @Test
    void existsById_ShouldReturnCorrectStatus() {
        Film film = filmRepository.create(testFilm1).orElseThrow();

        assertTrue(filmRepository.existsById(film.getId()));
        assertFalse(filmRepository.existsById(999));
    }

}
