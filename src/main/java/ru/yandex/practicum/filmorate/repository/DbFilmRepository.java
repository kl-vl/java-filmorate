package ru.yandex.practicum.filmorate.repository;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.FilmCreateFailed;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.mappers.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Primary
@Repository
@RequiredArgsConstructor
public class DbFilmRepository implements FilmRepository {

    private static final int DEFAULT_FILM_LIMIT = 100;
    private static final String SQL_INSERT_FILM = "INSERT INTO \"film\" (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_FILM = "UPDATE \"film\" SET name = ?, description = ?, release_date = ?, " +
            "duration = ?, mpa_id = ? WHERE id = ?";
    private static final String SQL_SELECT_FILM_BY_ID_WITH_DETAILS = """
            SELECT
                f.id, f.name, f.description, f.release_date, f.duration,
                m.id AS mpa_id, m.name AS mpa_name,
                g.id AS genre_id, g.name AS genre_name
            FROM "film" f
            LEFT JOIN "mpa" m ON f.mpa_id = m.id
            LEFT JOIN "film_genre" fg ON f.id = fg.film_id
            LEFT JOIN "genre" g ON fg.genre_id = g.id
            WHERE f.id = ?
            ORDER BY g.id ASC
            """;
    private static final String SQL_SELECT_FILMS_WITH_DETAILS = """
            SELECT
                f.id AS film_id,
                f.name AS film_name,
                f.description,
                f.release_date,
                f.duration,
                m.id AS mpa_id,
                m.name AS mpa_name,
                g.id AS genre_id,
                g.name AS genre_name
            FROM "film" f
            LEFT JOIN "mpa" m ON f.mpa_id = m.id
            LEFT JOIN "film_genre" fg ON f.id = fg.film_id
            LEFT JOIN "genre" g ON fg.genre_id = g.id
            ORDER BY f.id, g.id
            LIMIT ?
            """;
    private static final String SQL_SELECT_FILM_LIMIT = "SELECT * FROM \"film\" ORDER BY id LIMIT ?";
    private static final String SQL_SELECT_FILM_POPULAR = """
            SELECT
                f.id AS film_id, f.name, f.description,
                f.release_date, f.duration,
                m.id AS mpa_id, m.name AS mpa_name,
                g.id AS genre_id, g.name AS genre_name,
                COUNT(fl.user_id) AS likes_count
            FROM "film" f
            LEFT JOIN "mpa" m ON f.mpa_id = m.id
            LEFT JOIN "film_genre" fg ON f.id = fg.film_id
            LEFT JOIN "genre" g ON fg.genre_id = g.id
            LEFT JOIN "film_like" fl ON f.id = fl.film_id
            GROUP BY f.id, m.id, g.id
            ORDER BY likes_count DESC
            LIMIT ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final DbMpaRepository mpaRepository;
    private final DbGenreRepository genreRepository;
    private final FilmRowMapper filmRowMapper;

    @Override
    @Transactional
    public Optional<Film> create(Film film) {

        validateMpa(film.getMpa());
        validateGenres(film.getGenres());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    SQL_INSERT_FILM,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration().toMinutes());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Integer generatedId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        film.setId(generatedId);

        genreRepository.saveGenres(film, false);

        return Optional.ofNullable(getFilmWithDetails(generatedId).orElseThrow(() -> new FilmCreateFailed("Film not created")));
    }


    public Optional<Film> update(Film film) {
        if (film.getId() == null) {
            throw new FilmValidationException("Film ID must be provided for update");
        }
        if (!existsById(film.getId())) {
            throw new FilmNotFoundException("The Film with ID=%s not found".formatted(film.getId()));
        }

        validateMpa(film.getMpa());
        validateGenres(film.getGenres());

        jdbcTemplate.update(
                SQL_UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration().toMinutes(),
                film.getMpa().getId(),
                film.getId()
        );

        genreRepository.saveGenres(film, true);

        return Optional.ofNullable(getFilmWithDetails(film.getId()).orElseThrow(() -> new FilmCreateFailed("Film create failed")));
    }


    private void validateMpa(Mpa mpa) {
        if (mpaRepository.getMpaById(mpa.getId()).isEmpty()) {
            throw new MpaNotFoundException("MPA rating with id " + mpa.getId() + " not found");
        }
    }

    private void validateGenres(Set<Genre> genres) {
        Set<Integer> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        if (genreIds.size() != genres.size()) {
            throw new ValidationException("Duplicate genre ids found");
        }

        List<Integer> existingIds = genreRepository.findAllExistingIds(genreIds);

        Set<Integer> missingIds = genreIds.stream()
                .filter(id -> !existingIds.contains(id))
                .collect(Collectors.toSet());

        if (!missingIds.isEmpty()) {
            throw new GenreNotFoundException("Genres not found with ids: " + missingIds);
        }
    }

    public Optional<Film> getFilmWithDetails(Integer filmId) {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_FILM_BY_ID_WITH_DETAILS, rs -> {
            Map<Integer, Film> filmMap = new HashMap<>();
            while (rs.next()) {
                Integer currentId = rs.getInt("id");
                Film film = filmMap.get(currentId);

                if (film == null) {
                    film = new Film();
                    film.setId(currentId);
                    film.setName(rs.getString("name"));
                    film.setDescription(rs.getString("description"));

                    Date releaseDate = rs.getDate("release_date");
                    if (releaseDate != null) {
                        film.setReleaseDate(releaseDate.toLocalDate());
                    }

                    int durationMinutes = rs.getInt("duration");
                    if (durationMinutes > 0) {
                        film.setDuration(Duration.ofMinutes(rs.getInt("duration")));
                    }

                    if (rs.getObject("mpa_id") != null) {
                        Mpa mpa = new Mpa(
                                rs.getInt("mpa_id"),
                                rs.getString("mpa_name")
                        );
                        film.setMpa(mpa);
                    }

                    //film.setGenres(new LinkedHashSet<>());
                    filmMap.put(currentId, film);
                }

                if (rs.getObject("genre_id") != null) {
                    Genre genre = new Genre(
                            rs.getInt("genre_id"),
                            rs.getString("genre_name")
                    );
                    film.getGenres().add(genre);
                }
            }
            return new ArrayList<>(filmMap.values());
        }, filmId);

        return films.isEmpty() ? Optional.empty() : Optional.of(films.get(0));
    }

    @Override
    public Optional<Film> getById(Integer id) {
        return getFilmWithDetails(id);
    }

    public boolean existsById(Integer id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM \"film\" WHERE id = ?",
                Integer.class,
                id
        );
        return count != null && count > 0;
    }

    @Override
    public List<Film> findAll() {
        return findAll(DEFAULT_FILM_LIMIT);
    }

    private List<Film> findAll(int limit) {
        Map<Integer, Film> filmsMap = new LinkedHashMap<>();

        jdbcTemplate.query(SQL_SELECT_FILMS_WITH_DETAILS, rs -> {
            int filmId = rs.getInt("film_id");
            Film film = filmsMap.get(filmId);

            if (film == null) {
                film = Film.builder()
                        .id(filmId)
                        .name(rs.getString("film_name"))
                        .description(rs.getString("description"))
                        .releaseDate(rs.getDate("release_date").toLocalDate())
                        .duration(Duration.ofMinutes(rs.getInt("duration")))
                        .mpa(new Mpa(
                                rs.getInt("mpa_id"),
                                rs.getString("mpa_name")))
                        .genres(new LinkedHashSet<>())
                        .build();
                filmsMap.put(filmId, film);
            }

            if (rs.getObject("genre_id") != null) {
                Genre genre = new Genre(
                        rs.getInt("genre_id"),
                        rs.getString("genre_name")
                );
                film.getGenres().add(genre);
            }
        }, limit);

        return new ArrayList<>(filmsMap.values());
    }

    public List<Film> getPopularFilms(int limit) {
        Map<Integer, Film> filmMap = new LinkedHashMap<>();

        jdbcTemplate.query(SQL_SELECT_FILM_POPULAR, rs -> {
            int filmId = rs.getInt("film_id");
            Film film = filmMap.get(filmId);

            if (film == null) {
                film = Film.builder()
                        .id(filmId)
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .releaseDate(rs.getDate("release_date").toLocalDate())
                        .duration(Duration.ofMinutes(rs.getInt("duration")))
                        .mpa(new Mpa(
                                rs.getInt("mpa_id"),
                                rs.getString("mpa_name")))
                        .genres(new LinkedHashSet<>())
                        .build();
                filmMap.put(filmId, film);
            }

            if (rs.getObject("genre_id") != null) {
                film.addGenre(new Genre(
                        rs.getInt("genre_id"),
                        rs.getString("genre_name")
                ));
            }
        }, limit);

        return new ArrayList<>(filmMap.values());
    }

    public boolean addLike(Integer filmId, Integer userId) {
        try {
            int updated = jdbcTemplate.update(
                    "INSERT INTO \"film_like\" (film_id, user_id) VALUES (?, ?)",
                    filmId,
                    userId
            );
            return updated > 0;
        } catch (Exception e) {
            // В случае нарушения уникальности (лайк уже существует)
            return false;
        }
    }

    public boolean removeLike(Integer filmId, Integer userId) {
        int updated = jdbcTemplate.update(
                "DELETE FROM \"film_like\" WHERE film_id = ? AND user_id = ?",
                filmId,
                userId
        );
        return updated > 0;
    }

}
