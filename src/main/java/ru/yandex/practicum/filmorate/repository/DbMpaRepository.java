package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.mappers.MpaRowMapper;

import java.util.List;
import java.util.Optional;

@Repository("dbMpaRepository")
@RequiredArgsConstructor
public class DbMpaRepository {

    private static final String SQL_SELECT_MPA_BY_ID = "SELECT id as mpa_id, name as mpa_name FROM \"mpa\" WHERE id = ?";
    private static final String SQL_SELECT_MPA_ORDER_BY_ID = "SELECT id as mpa_id, name as mpa_name FROM \"mpa\" ORDER BY id ASC";
    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    public Optional<Mpa> getMpaById(Integer id) {
        return jdbcTemplate.query(SQL_SELECT_MPA_BY_ID, mpaRowMapper, id).stream().findFirst();
    }

    public List<Mpa> getAllMpa() {
        return jdbcTemplate.query(SQL_SELECT_MPA_ORDER_BY_ID, mpaRowMapper);
    }
}
