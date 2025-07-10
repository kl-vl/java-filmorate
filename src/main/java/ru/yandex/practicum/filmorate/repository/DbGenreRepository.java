package ru.yandex.practicum.filmorate.repository;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository("dbGenreRepository")
@AllArgsConstructor
public class DbGenreRepository {

    private static final String SQL_SELECT_GENRE_BY_ID = "SELECT id as genre_id, name as genre_name FROM \"genre\" WHERE id = ?";
    private static final String SQL_SELECT_GENRE_BY_FILM_ID = """
        SELECT g.id as genre_id, g.name as genre_name
        FROM "genre" g
        JOIN "film_genre" fg ON g.id = fg.genre_id
        WHERE fg.film_id = ?
        ORDER BY g.id ASC
        """;
    private static final String SQL_DELETE_FILM_GENRE_BY_FILM_ID = "DELETE FROM \"film_genre\" WHERE film_id = ?";
    private static final String SQL_INSERT_FILM_GENRE = "INSERT INTO \"film_genre\" (film_id, genre_id) VALUES (?, ?)";
    private static final String SQL_SELECT_GENRE_ORDER_BY_ID = "SELECT id as genre_id, name as genre_name FROM \"genre\" ORDER BY id ASC";

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    public Optional<Genre> findById(Integer id) {
        List<Genre> genres = jdbcTemplate.query(SQL_SELECT_GENRE_BY_ID, genreRowMapper, id);
        return genres.stream().findFirst();
    }

    public List<Genre> findByFilmId(Integer filmId) {
        return jdbcTemplate.query(SQL_SELECT_GENRE_BY_FILM_ID, genreRowMapper, filmId);
    }

    public void saveGenres(Film film,boolean isUpdate) {
        if (isUpdate) {
            jdbcTemplate.update(SQL_DELETE_FILM_GENRE_BY_FILM_ID, film.getId());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Object[]> batchArgs = film.getGenres().stream()
                    .map(genre -> new Object[]{film.getId(), genre.getId()})
                    .collect(Collectors.toList());
            jdbcTemplate.batchUpdate(SQL_INSERT_FILM_GENRE, batchArgs);
        }
    }

    public List<Genre> findAll() {
        return jdbcTemplate.query(SQL_SELECT_GENRE_ORDER_BY_ID, genreRowMapper);
    }


    public List<Integer> findAllExistingIds(Set<Integer> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT id FROM \"genre\" WHERE id IN (" + placeholders + ")";

        return jdbcTemplate.queryForList(
                sql,
                Integer.class,
                ids.toArray()
        );
    }

}
