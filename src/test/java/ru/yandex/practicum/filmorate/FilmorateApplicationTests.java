package ru.yandex.practicum.filmorate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.enums.SearchBy;
import ru.yandex.practicum.filmorate.enums.EventOperation;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.SearchCriteria;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.DbDirectorRepository;
import ru.yandex.practicum.filmorate.repository.DbEventRepository;
import ru.yandex.practicum.filmorate.repository.DbFilmRepository;
import ru.yandex.practicum.filmorate.repository.DbGenreRepository;
import ru.yandex.practicum.filmorate.repository.DbMpaRepository;
import ru.yandex.practicum.filmorate.repository.DbReviewRepository;
import ru.yandex.practicum.filmorate.repository.DbUserRepository;
import ru.yandex.practicum.filmorate.repository.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.RecommendationService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        DbUserRepository.class,
        DbFilmRepository.class,
        DbGenreRepository.class,
        DbMpaRepository.class,
        DbDirectorRepository.class,
        DbReviewRepository.class,
        DbEventRepository.class,
        FilmRowMapper.class,
        UserRowMapper.class,
        GenreRowMapper.class,
        MpaRowMapper.class,
        DirectorRowMapper.class,
        ReviewRowMapper.class,
        EventRowMapper.class,
        UserService.class,
        FilmService.class,
        RecommendationService.class
})

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

    @Autowired
    private DbReviewRepository reviewRepository;

    @Autowired
    private DbDirectorRepository directorRepository;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private DbEventRepository eventRepository;

    private User testUser1;
    private User testUser2;
    private User testUser3;
    private Film testFilm1;
    private Film testFilm2;
    private Film testFilm3;
    private Director testDirector1;
    private Director testDirector2;
    private Review testReview1;
    private Review testReview2;
    private Review testReview3;

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

        testUser3 = User.builder()
                .email("test3@mail.com")
                .login("testLogin3")
                .name("Test Name 3")
                .birthday(LocalDate.of(2003, 3, 3))
                .build();

        testFilm1 = Film.builder()
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

        testFilm2 = Film.builder()
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
        jdbcTemplate.execute("DELETE FROM \"review_likes\"");
        jdbcTemplate.execute("DELETE FROM \"user\"");
        jdbcTemplate.execute("DELETE FROM \"film\"");
        jdbcTemplate.execute("DELETE FROM \"director\"");
        jdbcTemplate.execute("DELETE FROM \"film_director\"");

        testDirector1 = directorRepository.create(
                Director.builder().name("Christopher Nolan").build()
        ).orElseThrow();
        testDirector2 = directorRepository.create(
                Director.builder().name("Quentin Tarantino").build()
        ).orElseThrow();

        jdbcTemplate.execute("DELETE FROM \"review\"");
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

        List<Film> popularFilms = filmRepository.getPopularFilms(1, 2001, 1);

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

    @Test
    void findById_shouldReturnDirectorWhenExists() {
        Optional<Director> result = directorRepository.findById(testDirector1.getId());

        assertAll(
                () -> assertTrue(result.isPresent(), "Director should be present"),
                () -> assertEquals(testDirector1, result.get(), "Returned director should match expected")
        );
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        Optional<Director> result = directorRepository.findById(999);

        assertFalse(result.isPresent(), "Director should not be present");
    }

    @Test
    void findAll_shouldReturnAllDirectors() {
        List<Director> result = directorRepository.findAll();

        assertEquals(2, result.size(), "Should return 2 directors");
    }

    @Test
    void save_shouldCreateNewDirectorWithGeneratedId() {
        Director newDirector = Director.builder()
                .name("Martin Scorsese")
                .build();

        Director savedDirector = directorRepository.create(newDirector).orElseThrow();
        Optional<Director> retrievedDirector = directorRepository.findById(savedDirector.getId());

        assertAll(
                () -> assertNotNull(savedDirector.getId(), "ID should be generated"),
                () -> assertEquals(newDirector.getName(), savedDirector.getName(), "Names should match"),
                () -> assertTrue(retrievedDirector.isPresent(), "Director should be retrievable"),
                () -> assertEquals(savedDirector, retrievedDirector.get(), "Saved and retrieved directors should match")
        );
    }

    @Test
    void update_shouldModifyExistingDirector() {
        Director updated = Director.builder()
                .id(testDirector1.getId())
                .name("Chris Nolan")
                .build();

        Director result = directorRepository.update(updated).orElseThrow();
        Optional<Director> retrieved = directorRepository.findById(testDirector1.getId());

        assertAll(
                () -> assertEquals(updated, result, "Returned director should match updated"),
                () -> assertTrue(retrieved.isPresent(), "Director should exist"),
                () -> assertEquals("Chris Nolan", retrieved.get().getName(), "Name should be updated"),
                () -> assertEquals(testDirector1.getId(), retrieved.get().getId(), "ID should remain the same")
        );
    }

    @Test
    void deleteById_shouldRemoveDirector() {
        boolean deleted = directorRepository.deleteById(testDirector1.getId());
        Optional<Director> result = directorRepository.findById(testDirector1.getId());

        assertAll(
                () -> assertTrue(deleted, "Should return true when deleted"),
                () -> assertFalse(result.isPresent(), "Director should not exist after deletion"),
                () -> assertEquals(1, directorRepository.findAll().size(), "Should only have one director left")
        );
    }

    @Test
    void existsById_shouldReturnCorrectStatus() {
        assertAll(
                () -> assertTrue(directorRepository.existsById(testDirector1.getId())),
                () -> assertFalse(directorRepository.existsById(999))
        );
    }

    @Test
    void save_shouldThrowExceptionWhenNameIsNull() {
        Director invalidDirector = Director.builder().name(null).build();

        assertThrows(DataIntegrityViolationException.class,
                () -> directorRepository.create(invalidDirector));
    }

    @Test
    void getCommonFilms_ShouldReturnMutualLikedFilms() {
        User user1 = userRepository.create(testUser1).orElseThrow();
        User user2 = userRepository.create(testUser2).orElseThrow();

        Film film1 = filmRepository.create(testFilm1).orElseThrow();
        Film film2 = filmRepository.create(testFilm2).orElseThrow();

        filmRepository.addLike(film1.getId(), user1.getId());
        filmRepository.addLike(film2.getId(), user2.getId());

        List<Film> noCommons = filmRepository.getCommonFilms(user1.getId(), user2.getId());
        assertTrue(noCommons.isEmpty(), "Общих фильмов быть не должно");

        filmRepository.addLike(film2.getId(), user1.getId());
        filmRepository.addLike(film1.getId(), user2.getId());

        List<Film> commons = filmRepository.getCommonFilms(user1.getId(), user2.getId());
        assertEquals(2, commons.size(), "Должны быть 2 общих фильма");

        Set<Integer> ids = commons.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());
        assertTrue(ids.contains(film1.getId()), "Должен быть Film 1");
        assertTrue(ids.contains(film2.getId()), "Должен быть Film 2");
    }

    @Test
    void userDeleting_MustUndergoCheck() {
        Optional<User> createUser = userRepository.create(testUser1);

        assertNotNull(createUser.get());

        int userId = createUser.get().getId();

        userRepository.removeUserById(userId);

        Optional<User> optionalIsNull = userRepository.getById(userId);

        assertTrue(optionalIsNull.isEmpty());
    }

    @Test
    void removal_OfFilmMustUndergoCheck() {
        Optional<Film> createFilm = filmRepository.create(testFilm1);

        assertNotNull(createFilm.get());

        int filmId = createFilm.get().getId();
        boolean res = filmRepository.removeFilmById(filmId);
        Optional<Film> optionalIsEmpty = filmRepository.getById(filmId);

        assertTrue(optionalIsEmpty.isEmpty());
    }

    @Test
    void shouldCreateReview() {
        createReviews();

        assertThat(testReview1).isNotNull()
                .hasFieldOrPropertyWithValue("content", "Good film")
                .hasFieldOrPropertyWithValue("isPositive", true)
                .hasFieldOrPropertyWithValue("useful", 0)
                .hasFieldOrPropertyWithValue("userId", testUser3.getId())
                .hasFieldOrPropertyWithValue("filmId", testFilm1.getId());
        assertThat(testReview1.getReviewId()).isNotNull();

    }

    @Test
    public void shouldUpdateReview() {
        createReviews();
        Review review = testReview1;
        review.setContent("Very Good film");
        testReview1 = reviewRepository.update(review).orElseThrow();

        assertThat(testReview1).isNotNull()
                .hasFieldOrPropertyWithValue("content", "Very Good film");
    }

    @Test
    public void shouldGetReviewById() {
        createReviews();
        Review review = reviewRepository.getReviewById(2).orElseThrow();

        assertThat(review).isNotNull()
                .hasFieldOrPropertyWithValue("content", "Bad film");
    }

    @Test
    public void shouldDeleteReview() {
        createReviews();
        Optional<Review> optReview = reviewRepository.getReviewById(2);

        assertTrue(optReview.isPresent());

        reviewRepository.deleteReview(2);
        optReview = reviewRepository.getReviewById(2);

        assertTrue(optReview.isEmpty());
    }

    @Test
    public void shouldGetAllReviews() {
        createReviews();
        Integer filmId = testFilm2.getId();
        List<Review> reviewList = reviewRepository.getAllByFilmIdWithLimit(filmId, 10);

        assertEquals(2, reviewList.size());

        Review review = reviewList.get(0);

        Assertions.assertThat(review).isNotNull()
                .hasFieldOrPropertyWithValue("content", "Bad film")
                .hasFieldOrPropertyWithValue("isPositive", false)
                .hasFieldOrPropertyWithValue("useful", 0)
                .hasFieldOrPropertyWithValue("userId", testUser3.getId())
                .hasFieldOrPropertyWithValue("filmId", testFilm2.getId());

        review = reviewList.get(1);

        Assertions.assertThat(review).isNotNull()
                .hasFieldOrPropertyWithValue("content", "Not Bad film")
                .hasFieldOrPropertyWithValue("isPositive", true)
                .hasFieldOrPropertyWithValue("useful", 0)
                .hasFieldOrPropertyWithValue("userId", testUser2.getId())
                .hasFieldOrPropertyWithValue("filmId", testFilm2.getId());
    }

    @Test
    public void shouldLikeDislike() {
        createReviews();
        Integer reviewId1 = testReview1.getReviewId();
        Review review = reviewRepository.getReviewById(reviewId1).orElseThrow();

        assertEquals(0, review.getUseful());

        // первый лайк
        reviewRepository.like(testUser1.getId(), reviewId1, 1);
        review = reviewRepository.getReviewById(reviewId1).orElseThrow();

        assertEquals(1, review.getUseful());

        // второй лайк
        reviewRepository.like(testUser2.getId(), reviewId1, 1);
        review = reviewRepository.getReviewById(reviewId1).orElseThrow();

        assertEquals(2, review.getUseful());

        // дизлайк
        reviewRepository.dislike(testUser2.getId(), reviewId1, 1);
        review = reviewRepository.getReviewById(reviewId1).orElseThrow();

        assertEquals(1, review.getUseful());

        // снял дизлайк
        reviewRepository.dislikeOff(testUser2.getId(), reviewId1);
        review = reviewRepository.getReviewById(reviewId1).orElseThrow();

        assertEquals(2, review.getUseful());

        // снял лайк
        reviewRepository.likeOff(testUser1.getId(), reviewId1);
        review = reviewRepository.getReviewById(reviewId1).orElseThrow();

        assertEquals(1, review.getUseful());
    }

    private void createReviews() {
        jdbcTemplate.update("ALTER TABLE \"user\" ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE \"film\" ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE \"review\" ALTER COLUMN id RESTART WITH 1");
        User user3 = userRepository.create(testUser3).orElseThrow();
        User user2 = userRepository.create(testUser2).orElseThrow();
        User user1 = userRepository.create(testUser1).orElseThrow();
        Film film1 = filmRepository.create(testFilm1).orElseThrow();
        Review review1 = Review.builder()
                .content("Good film")
                .filmId(film1.getId())
                .userId(user3.getId())
                .isPositive(true)
                .build();
        testReview1 = reviewRepository.create(review1).orElseThrow();

        Event newEvent = Event.builder()
                .eventType(EventType.REVIEW)
                .operation(EventOperation.ADD)
                .userId(testReview1.getUserId())
                .entityId(testReview1.getReviewId())
                .build();
        Optional<Event> optEvent = eventRepository.addEvent(newEvent);

        Film film2 = filmRepository.create(testFilm2).orElseThrow();
        Review review2 = Review.builder()
                .content("Bad film")
                .filmId(film2.getId())
                .userId(user3.getId())
                .isPositive(false)
                .build();
        testReview2 = reviewRepository.create(review2).orElseThrow();

        newEvent = Event.builder()
                .eventType(EventType.REVIEW)
                .operation(EventOperation.ADD)
                .userId(testReview2.getUserId())
                .entityId(testReview2.getReviewId())
                .build();
        optEvent = eventRepository.addEvent(newEvent);

        Review review3 = Review.builder()
                .content("Not Bad film")
                .filmId(film2.getId())
                .userId(user2.getId())
                .isPositive(true)
                .build();
        testReview3 = reviewRepository.create(review3).orElseThrow();

        newEvent = Event.builder()
                .eventType(EventType.REVIEW)
                .operation(EventOperation.ADD)
                .userId(testReview3.getUserId())
                .entityId(testReview3.getReviewId())
                .build();
        optEvent = eventRepository.addEvent(newEvent);
    }

    @Test
    public void shouldAddToEventFeed() {
        createReviews();
        Integer userId3 = testUser3.getId();
        List<Event> events = eventRepository.findAllByUserId(userId3,"asc");

        assertEquals(2, events.size());

        Assertions.assertThat(events.getFirst()).isNotNull()
                .hasFieldOrPropertyWithValue("userId", userId3)
                .hasFieldOrPropertyWithValue("eventType", EventType.REVIEW)
                .hasFieldOrPropertyWithValue("operation", EventOperation.ADD)
                .hasFieldOrPropertyWithValue("entityId", testReview1.getReviewId());

        Review review = testReview1;
        review.setContent("Very Good film");
        testReview1 = reviewRepository.update(review).orElseThrow();
        Event newEvent = Event.builder()
                .eventType(EventType.REVIEW)
                .operation(EventOperation.UPDATE)
                .userId(testReview1.getUserId())
                .entityId(testReview1.getReviewId())
                .build();
        Optional<Event> optEvent = eventRepository.addEvent(newEvent);

        events = eventRepository.findAllByUserId(userId3,"desc");

        assertEquals(3, events.size());

        Assertions.assertThat(events.getFirst()).isNotNull()
                .hasFieldOrPropertyWithValue("userId", userId3)
                .hasFieldOrPropertyWithValue("eventType", EventType.REVIEW)
                .hasFieldOrPropertyWithValue("operation", EventOperation.UPDATE)
                .hasFieldOrPropertyWithValue("entityId", testReview1.getReviewId());

        Integer reviewId1 = testReview1.getReviewId();
        reviewRepository.deleteReview(reviewId1);
        newEvent = Event.builder()
                .eventType(EventType.REVIEW)
                .operation(EventOperation.REMOVE)
                .userId(userId3)
                .entityId(reviewId1)
                .build();
        optEvent = eventRepository.addEvent(newEvent);

        events = eventRepository.findAllByUserId(userId3,"desc");

        assertEquals(4, events.size());

        Assertions.assertThat(events.getFirst()).isNotNull()
                .hasFieldOrPropertyWithValue("userId", userId3)
                .hasFieldOrPropertyWithValue("eventType", EventType.REVIEW)
                .hasFieldOrPropertyWithValue("operation", EventOperation.REMOVE)
                .hasFieldOrPropertyWithValue("entityId", reviewId1);
    }

    @Test
    void recommendFor_NoRecommendationsWhenNoLikes() {
        // создаём двух пользователей, один фильм, но не ставим лайков
        User u1 = userRepository.create(testUser1).orElseThrow();
        User u2 = userRepository.create(testUser2).orElseThrow();
        filmRepository.create(testFilm1).orElseThrow();

        // ни у кого нет лайков → пусто
        List<Film> recs1 = recommendationService.recommendFor(u1.getId());
        assertTrue(recs1.isEmpty(), "Ожидаем пустой список, если у пользователя нет лайков");

        List<Film> recs2 = recommendationService.recommendFor(u2.getId());
        assertTrue(recs2.isEmpty(), "Ожидаем пустой список, если у всех нет лайков");
    }

    @Test
    void recommendFor_ShouldRecommendBasedOnMostCommonNeighbor() {
        // три пользователя и два фильма
        User u1 = userRepository.create(testUser1).orElseThrow(); // целевой
        User u2 = userRepository.create(testUser2).orElseThrow(); // сосед 1
        User u3 = userRepository.create(testUser3).orElseThrow(); // сосед 2
        Film f1 = filmRepository.create(testFilm1).orElseThrow();
        Film f2 = filmRepository.create(testFilm2).orElseThrow();

        // u1 лайкнул только f1
        filmRepository.addLike(f1.getId(), u1.getId());
        // u2 (первый сосед) лайкнул f1 и f2 → 1 общее с u1
        filmRepository.addLike(f1.getId(), u2.getId());
        filmRepository.addLike(f2.getId(), u2.getId());
        // u3 лайкнул только f1 → 1 общее, но у него нет кандидатов для рекомендации
        filmRepository.addLike(f1.getId(), u3.getId());

        // проверяем рекомендации для u1
        List<Film> recs = recommendationService.recommendFor(u1.getId());
        assertEquals(1, recs.size(), "Ожидаем ровно одну рекомендацию");
        assertEquals(f2.getId(), recs.get(0).getId(), "Должен порекомендовать только фильм f2");
    }


    @Nested
    class FilmSearchTests {
        @BeforeEach
        void setupDirectorData() {
            createTestDataForSearch();
        }

        private void createTestDataForSearch() {
            Mpa mpa = mpaRepository.getMpaById(1).orElseThrow();

            testDirector1 = directorRepository.create(
                    Director.builder()
                            .name("Энг Ли")
                            .build()
            ).orElseThrow();

            testDirector2 = directorRepository.create(
                    Director.builder()
                            .name("Ночной режиссер")
                            .build()
            ).orElseThrow();

            testFilm1 = filmRepository.create(Film.builder()
                    .name("Крадущийся тигр, затаившийся дракон")
                    .description("Фильм о боевых искусствах")
                    .releaseDate(LocalDate.of(2000, 5, 18))
                    .duration(Duration.ofMinutes(120))
                    .mpa(mpa)
                    .directors(Set.of(testDirector1))
                    .build()).orElseThrow();

            testFilm2 = filmRepository.create(Film.builder()
                    .name("Крадущийся в ночи")
                    .description("Триллер о ночных приключениях")
                    .releaseDate(LocalDate.of(2021, 3, 15))
                    .duration(Duration.ofMinutes(110))
                    .mpa(mpa)
                    .build()).orElseThrow();

            testFilm3 = filmRepository.create(Film.builder()
                    .name("Совсем другой фильм")
                    .description("Не содержит искомых слов")
                    .releaseDate(LocalDate.of(2022, 1, 10))
                    .duration(Duration.ofMinutes(90))
                    .mpa(mpa)
                    .build()).orElseThrow();
        }

        @Test
        void searchByTitle_shouldFindTwoFilmsWithWordKrad() {
            SearchCriteria criteria = new SearchCriteria("крад", SearchBy.TITLE);
            List<Film> result = filmRepository.searchFilms(criteria);

            assertEquals(2, result.size(), "Должно найти 2 фильма с 'крад' в названии");
            assertTrue(containsFilmWithName(result, "Крадущийся тигр, затаившийся дракон"));
            assertTrue(containsFilmWithName(result, "Крадущийся в ночи"));
        }

        @Test
        void searchByDirector_shouldFindFilmByDirectorEngLi() {
            SearchCriteria criteria = new SearchCriteria("Энг", SearchBy.DIRECTOR);
            List<Film> result = filmRepository.searchFilms(criteria);

            assertEquals(1, result.size());
            assertEquals(testFilm1.getName(), result.get(0).getName());
        }

        @Test
        void searchByBoth_shouldFindAllFilmsWithWordKrad() {
            SearchCriteria criteria = new SearchCriteria("крад", SearchBy.BOTH);
            List<Film> result = filmRepository.searchFilms(criteria);

            assertEquals(2, result.size());
            assertTrue(containsFilmWithName(result, "Крадущийся тигр, затаившийся дракон"));
            assertTrue(containsFilmWithName(result, "Крадущийся в ночи"));
        }

        @Test
        void searchByTitle_shouldBeCaseInsensitive() {
            SearchCriteria criteria = new SearchCriteria("КРАД", SearchBy.TITLE);
            List<Film> result = filmRepository.searchFilms(criteria);

            System.out.println(result);
            assertEquals(2, result.size());
        }

        @Test
        void search_shouldReturnEmptyListWhenNoMatches() {
            SearchCriteria criteria = new SearchCriteria("несуществующий", SearchBy.BOTH);
            List<Film> result = filmRepository.searchFilms(criteria);

            assertTrue(result.isEmpty());
        }

        private boolean containsFilmWithName(List<Film> films, String name) {
            return films.stream().anyMatch(f -> f.getName().equals(name));
        }
    }

}
