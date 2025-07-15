package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.mappers.DirectorRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Primary
@Repository("dbDirectorRepository")
@RequiredArgsConstructor
public class DbDirectorRepository {

    private static final String SQL_SELECT_DIRECTOR_BY_ID = "SELECT id AS director_id, name AS director_name FROM \"director\" WHERE id = ?";
    private static final String SQL_SELECT_DIRECTOR_BY_FILM_ID = """
        SELECT d.id AS director_id, d.name AS director_name
        FROM "director" d
        JOIN "film_director" fd ON d.id = fd.director_id
        WHERE fd.film_id = ?
        ORDER BY d.id ASC
        """;
    private static final String SQL_DELETE_FILM_DIRECTOR_BY_FILM_ID = "DELETE FROM \"film_director\" WHERE film_id = ?";
    private static final String SQL_INSERT_FILM_DIRECTOR = "INSERT INTO \"film_director\" (film_id, director_id) VALUES (?, ?)";
    private static final String SQL_SELECT_DIRECTOR_ORDER_BY_ID = "SELECT id AS director_id, name AS director_name FROM \"director\" ORDER BY id ASC";
    private static final String SQL_INSERT_DIRECTOR = "INSERT INTO \"director\" (name) VALUES (?)";
    private static final String SQL_UPDATE_DIRECTOR = "UPDATE \"director\" SET name = ? WHERE id = ?";
    private static final String SQL_DELETE_DIRECTOR = "DELETE FROM \"director\" WHERE id = ?";
    private static final String SQL_EXISTS_DIRECTOR_BY_ID = "SELECT COUNT(*) FROM \"director\" WHERE id = ?";
    private static final String SQL_DELETE_FILM_DIRECTORS = "DELETE FROM \"film_director\" WHERE film_id = ?";


    private final JdbcTemplate jdbcTemplate;
    private final DirectorRowMapper directorRowMapper;

    public List<Director> findAll() {
        return jdbcTemplate.query(SQL_SELECT_DIRECTOR_ORDER_BY_ID, directorRowMapper);
    }

    public Optional<Director> findById(Integer id) {
        List<Director> directors = jdbcTemplate.query(SQL_SELECT_DIRECTOR_BY_ID, directorRowMapper, id);
        return directors.stream().findFirst();
    }

    public boolean existsById(Integer id) {
        Integer count = jdbcTemplate.queryForObject(SQL_EXISTS_DIRECTOR_BY_ID, Integer.class, id);
        return count != null && count > 0;
    }

    public List<Director> findByFilmId(Integer filmId) {
        return jdbcTemplate.query(SQL_SELECT_DIRECTOR_BY_FILM_ID, directorRowMapper, filmId);
    }

    @Transactional
    public void saveFilmDirectors(Film film, boolean isUpdate) {
        if (isUpdate) {
            jdbcTemplate.update(SQL_DELETE_FILM_DIRECTORS, film.getId());
        }

            List<Object[]> batchArgs = film.getDirectors().stream()
                    .map(d -> new Object[]{film.getId(), d.getId()})
                    .collect(Collectors.toList());

            jdbcTemplate.batchUpdate(SQL_INSERT_FILM_DIRECTOR, batchArgs);
    }

    public List<Integer> findAllExistingIds(Set<Integer> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT id FROM \"director\" WHERE id IN (" + placeholders + ")";

        return jdbcTemplate.queryForList(
                sql,
                Integer.class,
                ids.toArray()
        );
    }

    @Transactional
    public Optional<Director> create(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int affectedRows = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    SQL_INSERT_DIRECTOR,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        if (affectedRows == 0 || keyHolder.getKey() == null) {
            return Optional.empty();
        }

        director.setId(keyHolder.getKey().intValue());
        return Optional.of(director);
    }

    @Transactional
    public Optional<Director> update(Director director) {
        int updated = jdbcTemplate.update(SQL_UPDATE_DIRECTOR,
                director.getName(),
                director.getId());
        return updated == 1 ? Optional.of(director) : Optional.empty();
    }

    @Transactional
    public boolean deleteById(int id) {
        int rowsAffected = jdbcTemplate.update(SQL_DELETE_DIRECTOR, id);
        return rowsAffected > 0;
    }

    public void validateDirectors(Set<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }

        Set<Integer> directorIds = directors.stream()
                .map(Director::getId)
                .collect(Collectors.toSet());

        List<Integer> existingIds = findAllExistingIds(directorIds);

        if (existingIds.size() != directorIds.size()) {
            throw new DirectorNotFoundException("One or more directors not found");
        }
    }

}
