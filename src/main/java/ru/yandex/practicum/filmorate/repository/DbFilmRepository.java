package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.FilmAccessException;
import ru.yandex.practicum.filmorate.exception.FilmCreateFailed;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SearchCriteria;
import ru.yandex.practicum.filmorate.repository.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.MpaRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
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
                f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
                m.id AS mpa_id, m.name AS mpa_name,
                g.id AS genre_id, g.name AS genre_name,
                d.id AS director_id, d.name AS director_name
            FROM "film" f
            LEFT JOIN "mpa" m ON f.mpa_id = m.id
            LEFT JOIN "film_genre" fg ON f.id = fg.film_id
            LEFT JOIN "genre" g ON fg.genre_id = g.id
            LEFT JOIN "film_director" fd ON f.id = fd.film_id
            LEFT JOIN "director" d ON fd.director_id = d.id
            WHERE f.id = ?
            ORDER BY g.id ASC
            """;
    private static final String SQL_SELECT_FILMS_WITH_DETAILS = """
            SELECT
                f.id AS film_id, f.name AS film_name, f.description, f.release_date,f.duration,
                m.id AS mpa_id, m.name AS mpa_name,
                g.id AS genre_id, g.name AS genre_name,
                d.id AS director_id, d.name AS director_name
            FROM "film" f
            LEFT JOIN "mpa" m ON f.mpa_id = m.id
            LEFT JOIN "film_genre" fg ON f.id = fg.film_id
            LEFT JOIN "genre" g ON fg.genre_id = g.id
            LEFT JOIN "film_director" fd ON f.id = fd.film_id
            LEFT JOIN "director" d ON fd.director_id = d.id
            ORDER BY f.id, g.id
            LIMIT ?
            """;
    private static final String SQL_SELECT_FILM_POPULAR = """
            WITH film_popularity AS (
                SELECT
                    f.id,
                    COUNT(DISTINCT fl.user_id) AS popularity
                FROM "film" f
                LEFT JOIN "film_like" fl ON f.id = fl.film_id
                GROUP BY f.id
            ),
            top_film_ids AS (
                SELECT id 
                FROM film_popularity 
                ORDER BY popularity DESC 
                LIMIT ?
            )
            SELECT 
                f.id AS film_id,
                f.name AS film_name,
                f.description,
                f.release_date,
                f.duration,
                m.id AS mpa_id,
                m.name AS mpa_name,
                g.id AS genre_id,
                g.name AS genre_name,
                d.id AS director_id,
                d.name AS director_name,
                fp.popularity
            FROM top_film_ids tfi
            JOIN "film" f ON tfi.id = f.id
            JOIN film_popularity fp ON f.id = fp.id
            JOIN "mpa" m ON f.mpa_id = m.id
            LEFT JOIN "film_genre" fg ON f.id = fg.film_id
            LEFT JOIN "genre" g ON fg.genre_id = g.id
            LEFT JOIN "film_director" fd ON f.id = fd.film_id
            LEFT JOIN "director" d ON fd.director_id = d.id
            ORDER BY fp.popularity DESC, f.id
            """;
    private static final String SQL_SELECT_FILMS_BY_DIRECTOR_LIKES = """
            SELECT
                f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
                m.id AS mpa_id, m.name AS mpa_name,
                g.id AS genre_id, g.name AS genre_name,
                d.id AS director_id, d.name AS director_name,
                (SELECT COUNT(*) FROM "film_like" WHERE film_id = f.id) AS popularity
            FROM "film" f
            JOIN "film_director" fd ON f.id = fd.film_id
            LEFT JOIN "mpa" m ON f.mpa_id = m.id
            LEFT JOIN "film_genre" fg ON f.id = fg.film_id
            LEFT JOIN "genre" g ON fg.genre_id = g.id
            LEFT JOIN "director" d ON fd.director_id = d.id
            WHERE fd.director_id = ?
            ORDER BY popularity DESC
            """;
    private static final String SQL_SELECT_FILMS_BY_DIRECTOR_YEAR = """
            SELECT
                f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
                m.id AS mpa_id, m.name AS mpa_name,
                g.id AS genre_id, g.name AS genre_name,
                d.id AS director_id, d.name AS director_name
            FROM "film" f
            JOIN "film_director" fd ON f.id = fd.film_id
            LEFT JOIN "mpa" m ON f.mpa_id = m.id
            LEFT JOIN "film_genre" fg ON f.id = fg.film_id
            LEFT JOIN "genre" g ON fg.genre_id = g.id
            LEFT JOIN "director" d ON fd.director_id = d.id
            WHERE fd.director_id = ?
            ORDER BY f.release_date
            """;
    private static final String SQL_INSERT_FILM_LIKE = "INSERT INTO \"film_like\" (film_id, user_id) VALUES (?, ?)";
    private static final String SQL_REMOVE_FILM_BY_ID = "DELETE FROM \"film\" WHERE id = ?";
    private static final String SQL_SELECT_COMMON_FILMS = """
            SELECT
                f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
                m.id AS mpa_id, m.name AS mpa_name,
                g.id AS genre_id, g.name AS genre_name,
                d.id AS director_id, d.name AS director_name,
                (SELECT COUNT(*) FROM "film_like" fl WHERE fl.film_id = f.id) AS popularity
            FROM "film" f
            LEFT JOIN "mpa" m ON f.mpa_id = m.id
            LEFT JOIN "film_genre" fg ON f.id = fg.film_id
            LEFT JOIN "genre" g ON fg.genre_id = g.id
            JOIN "film_like" fl1 ON f.id = fl1.film_id AND fl1.user_id = ?
            JOIN "film_like" fl2 ON f.id = fl2.film_id AND fl2.user_id = ?
            LEFT JOIN "film_director" fd ON f.id = fd.film_id
            LEFT JOIN "director" d ON fd.director_id = d.id
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration, m.id, m.name, g.id, g.name
            ORDER BY popularity DESC
            """;
    private static final String SEARCH_BY_TITLE_QUERY = """
            SELECT
                 f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
                 m.id AS mpa_id, m.name AS mpa_name,
                 g.id AS genre_id, g.name AS genre_name,
                 d.id AS director_id, d.name AS director_name,
                (SELECT COUNT(*) FROM "film_like" WHERE film_id = f.id) AS popularity
            FROM "film" f
            LEFT JOIN "mpa" m ON f.mpa_id = m.id
            LEFT JOIN "film_genre" fg ON f.id = fg.film_id
            LEFT JOIN "genre" g ON fg.genre_id = g.id
            LEFT JOIN "film_director" fd ON f.id = fd.film_id
            LEFT JOIN "director" d ON fd.director_id = d.id
            WHERE LOWER(f.name) LIKE ?
            ORDER BY popularity DESC
            """;
    private static final String SEARCH_BY_DIRECTOR_QUERY = """
            SELECT
                f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
                m.id AS mpa_id, m.name AS mpa_name,
                g.id AS genre_id, g.name AS genre_name,
                d.id AS director_id, d.name AS director_name,
                (SELECT COUNT(*) FROM "film_like" WHERE film_id = f.id) AS popularity
            FROM "film" f
            LEFT JOIN "mpa" m ON f.mpa_id = m.id
            LEFT JOIN "film_genre" fg ON f.id = fg.film_id
            LEFT JOIN "genre" g ON fg.genre_id = g.id
            JOIN "film_director" fd ON f.id = fd.film_id
            JOIN "director" d ON fd.director_id = d.id
            WHERE LOWER(d.name) LIKE ?
            ORDER BY popularity DESC
            """;
    private static final String SEARCH_BY_BOTH_QUERY = """
            SELECT DISTINCT
                f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
                m.id AS mpa_id, m.name AS mpa_name,
                g.id AS genre_id, g.name AS genre_name,
                d.id AS director_id, d.name AS director_name,
                (SELECT COUNT(*) FROM "film_like" WHERE film_id = f.id) AS popularity
            FROM "film" f
            LEFT JOIN "mpa" m ON f.mpa_id = m.id
            LEFT JOIN "film_genre" fg ON f.id = fg.film_id
            LEFT JOIN "genre" g ON fg.genre_id = g.id
            LEFT JOIN "film_director" fd ON f.id = fd.film_id
            LEFT JOIN "director" d ON fd.director_id = d.id
            WHERE LOWER(f.name) LIKE ? OR LOWER(d.name) LIKE ?
            ORDER BY popularity DESC
            """;
    private static final String SQL_SELECT_FILM_BY_ID = "SELECT COUNT(*) FROM \"film\" WHERE id = ?";
    private static final String SQL_DELETE_FILM_LIKE_BY_FILM_AND_USER = "DELETE FROM \"film_like\" WHERE film_id = ? AND user_id = ?";

    private static final String FIND_LIKED_FILM_IDS_BY_USER = "SELECT film_id FROM \"film_like\" WHERE user_id = ?";
    private static final String COUNT_LIKES_BY_FILM_ID = "SELECT COUNT(*) FROM \"film_like\" WHERE film_id = ?";
    private static final String SQL_BEST_FILMS_OF_GENRE_AND_YEAR = """
            WITH filtered_films AS (
                SELECT DISTINCT f.id
                FROM "film" f
                JOIN "film_genre" fg ON f.id = fg.film_id
                WHERE EXTRACT(YEAR FROM f.release_date) = ? 
                  AND fg.genre_id = ?
            )
            SELECT 
                f.id AS film_id,
                f.name AS film_name,
                f.description,
                f.release_date,
                EXTRACT(YEAR FROM f.release_date) AS release_year,
                f.duration,
                m.id AS mpa_id,
                m.name AS mpa_name,
                g.id AS genre_id,
                g.name AS genre_name,
                d.id AS director_id, 
                d.name AS director_name,
                COUNT(DISTINCT fl.user_id) AS film_like
            FROM "film" f
            JOIN filtered_films ff ON f.id = ff.id
            LEFT JOIN "film_genre" fg ON f.id = fg.film_id
            LEFT JOIN "genre" g ON fg.genre_id = g.id
            JOIN "mpa" m ON f.mpa_id = m.id
            LEFT JOIN "film_like" fl ON f.id = fl.film_id
            LEFT JOIN "film_director" fd ON f.id = fd.film_id
            LEFT JOIN "director" d ON fd.director_id = d.id
            GROUP BY 
                f.id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                m.id,
                m.name,
                g.id,
                g.name,
                d.id,
                d.name
            ORDER BY film_like DESC
            """;
    private static final String SQL_FILMS_OF_YEAR = """
            SELECT g.id AS genre_id,
                   g.name AS genre_name,
                   m.id AS mpa_id,
                   m.name AS mpa_name,
                   f.id AS film_id,
                   f.name AS film_name,
                   f.description,
                   f.release_date,
                   EXTRACT(YEAR FROM f.release_date) AS release_year,
                   f.duration,
                   d.id AS director_id, d.name AS director_name,
                   COUNT(DISTINCT fl.user_id) AS film_like
            FROM "film" AS f
            LEFT JOIN "film_genre" AS fg ON f.id = fg.film_id
            LEFT JOIN "genre" AS g ON fg.genre_id = g.id
            JOIN "mpa" AS m ON f.mpa_id = m.id
            LEFT JOIN "film_like" AS fl ON f.id = fl.film_id
            LEFT JOIN "film_director" fd ON f.id = fd.film_id
            LEFT JOIN "director" d ON fd.director_id = d.id
            WHERE EXTRACT(YEAR FROM f.release_date) = ?
            GROUP BY g.id,
                     g.name,
                     m.id,
                     m.name,
                     f.id,
                     f.name,
                     f.description,
                     f.release_date,
                     f.duration
            ORDER BY film_like DESC
            """;
    private static final String SQL_FILMS_OF_GENRE = """
                WITH films_filtered AS (
                   SELECT DISTINCT f.id
                   FROM "film" f
                   JOIN "film_genre" fg ON f.id = fg.film_id
                   WHERE fg.genre_id = ?
               )
               SELECT
                   f.id AS film_id,
                   f.name AS film_name,
                   f.description,
                   f.release_date,
                   f.duration,
                   m.id AS mpa_id,
                   m.name AS mpa_name,
                   g.id AS genre_id,
                   g.name AS genre_name,
                   d.id AS director_id,
                   d.name AS director_name,
                   COUNT(DISTINCT fl.user_id) AS film_like
               FROM "film" f
               JOIN films_filtered ff ON f.id = ff.id
               LEFT JOIN "film_like" fl ON f.id = fl.film_id
               LEFT JOIN "mpa" m ON f.mpa_id = m.id
               LEFT JOIN "film_genre" fg ON f.id = fg.film_id
               LEFT JOIN "genre" g ON fg.genre_id = g.id
               LEFT JOIN "film_director" fd ON f.id = fd.film_id
               LEFT JOIN "director" d ON fd.director_id = d.id
               GROUP BY
                   f.id, f.name, f.description, f.release_date, f.duration,
                   m.id, m.name, g.id, g.name, d.id, d.name
               ORDER BY film_like DESC
            """;
    private final JdbcTemplate jdbcTemplate;
    private final DbMpaRepository mpaRepository;
    private final DbGenreRepository genreRepository;
    private final DbDirectorRepository directorRepository;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final MpaRowMapper mpaRowMapper;
    private final DirectorRowMapper directorRowMapper;

    private List<Film> processFilmsQuery(String sql, Object... args) {
        Map<Integer, Film> filmsMap = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            Film film = filmsMap.computeIfAbsent(filmId, id -> {
                try {
                    Film newFilm = filmRowMapper.mapRow(rs, rs.getRow());
                    return newFilm;
                } catch (SQLException e) {
                    log.error("Error mapping film row. Film ID: {}, Row: {}", filmId, rs, e);
                    throw new FilmAccessException("Failed to map film data: " +
                            (e.getMessage() != null ? e.getMessage() : "Unknown SQL error"));
                }
            });

            try {
                filmRowMapper.addRelatedEntities(rs, rs.getRow(), film);
            } catch (SQLException e) {
                throw new FilmAccessException("Error adding relations", e);
            }
        }, args);

        return new ArrayList<>(filmsMap.values());
    }

    @Override
    @Transactional
    public Optional<Film> create(Film film) {
        mpaRepository.validateMpa(film.getMpa());
        genreRepository.validateGenres(film.getGenres());
        directorRepository.validateDirectors(film.getDirectors());

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
        directorRepository.saveFilmDirectors(film, false);

        return Optional.ofNullable(getFilmWithDetails(generatedId).orElseThrow(() -> new FilmCreateFailed("Film not created")));
    }


    public Optional<Film> update(Film film) {
        if (film.getId() == null) {
            throw new FilmValidationException("Film ID must be provided for update");
        }
        if (!existsById(film.getId())) {
            throw new FilmNotFoundException("The Film with ID=%s not found".formatted(film.getId()));
        }

        mpaRepository.validateMpa(film.getMpa());
        genreRepository.validateGenres(film.getGenres());
        directorRepository.validateDirectors(film.getDirectors());

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
        directorRepository.saveFilmDirectors(film, true);

        return Optional.ofNullable(getFilmWithDetails(film.getId()).orElseThrow(() -> new FilmCreateFailed("Film create failed")));
    }

    public Optional<Film> getFilmWithDetails(Integer filmId) {
        List<Film> films = processFilmsQuery(SQL_SELECT_FILM_BY_ID_WITH_DETAILS, filmId);
        return films.stream().findFirst();
    }

    @Override
    public Optional<Film> getById(Integer id) {
        return getFilmWithDetails(id);
    }

    public boolean existsById(Integer id) {
        Integer count = jdbcTemplate.queryForObject(SQL_SELECT_FILM_BY_ID, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public List<Film> findAll() {
        return findAll(DEFAULT_FILM_LIMIT);
    }

    private List<Film> findAll(int limit) {
        return processFilmsQuery(SQL_SELECT_FILMS_WITH_DETAILS, limit);
    }

    public List<Film> getPopularFilms(Integer limit, Integer year, Integer genreId) {
        String sql;
        Object[] params;

        if (year != null && genreId != null) {
            sql = SQL_BEST_FILMS_OF_GENRE_AND_YEAR;
            params = new Object[]{year, genreId};
        } else if (year != null) {
            sql = SQL_FILMS_OF_YEAR;
            params = new Object[]{year};
        } else if (genreId != null) {
            sql = SQL_FILMS_OF_GENRE;
            params = new Object[]{genreId};
        } else {
            sql = SQL_SELECT_FILM_POPULAR;
            params = new Object[]{limit != null && limit > 0 ? limit : 10}; // TODO default 10
        }
        return processFilmsQuery(sql, params);
    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        return processFilmsQuery(SQL_SELECT_COMMON_FILMS, userId, friendId);
    }

    @Transactional
    public boolean addLike(Integer filmId, Integer userId) {
        try {
            int updated = jdbcTemplate.update(SQL_INSERT_FILM_LIKE, filmId, userId);
            return updated > 0;
        } catch (Exception e) {
            // В случае нарушения уникальности (лайк уже существует)
            return false;
        }
    }

    @Transactional
    public boolean removeLike(Integer filmId, Integer userId) {
        int updated = jdbcTemplate.update(SQL_DELETE_FILM_LIKE_BY_FILM_AND_USER, filmId, userId);

        return updated > 0;
    }

    public List<Film> findFilmsByDirectorId(int directorId, String sortBy) {
        String sql = switch (sortBy) {
            case "year" -> SQL_SELECT_FILMS_BY_DIRECTOR_YEAR;
            case "likes" -> SQL_SELECT_FILMS_BY_DIRECTOR_LIKES;
            default -> throw new IllegalArgumentException("Invalid sort parameter: " + sortBy);
        };

        return processFilmsQuery(sql, directorId);
    }

    @Override
    @Transactional
    public boolean removeFilmById(Integer filmId) {
        int delete = jdbcTemplate.update(SQL_REMOVE_FILM_BY_ID, filmId);

        return delete > 0;
    }

    @Override
    public List<Film> searchFilms(SearchCriteria criteria) {
        String queryParam = "%" + criteria.getQuery() + "%";

        return switch (criteria.getSearchBy()) {
            case TITLE -> jdbcTemplate.query(SEARCH_BY_TITLE_QUERY, filmRowMapper, queryParam);
            case DIRECTOR -> jdbcTemplate.query(SEARCH_BY_DIRECTOR_QUERY, filmRowMapper, queryParam);
            case BOTH -> jdbcTemplate.query(SEARCH_BY_BOTH_QUERY, filmRowMapper, queryParam, queryParam);
        };
    }

    @Override
    public List<Integer> findLikedFilmIdsByUser(Integer userId) {
        return jdbcTemplate.queryForList(FIND_LIKED_FILM_IDS_BY_USER, Integer.class, userId);
    }

    @Override
    public int countLikesByFilmId(Integer filmId) {
        Integer cnt = jdbcTemplate.queryForObject(COUNT_LIKES_BY_FILM_ID, Integer.class, filmId);
        return cnt == null ? 0 : cnt;
    }
}
