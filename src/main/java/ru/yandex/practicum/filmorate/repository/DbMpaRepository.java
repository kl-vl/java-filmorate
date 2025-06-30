package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository("dbRatingRepository")
@RequiredArgsConstructor
public class DbMpaRepository {

    private static final String SQL_SELECT_MPA_BY_ID = "SELECT * FROM \"mpa\" WHERE id = ?";
    private static final String SQL_SELECT_MPA_ORDER_BY_ID = "SELECT * FROM \"mpa\" ORDER BY id ASC";
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Mpa> mpaRowMapper = (rs, rowNum) ->
            Mpa.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .build();

    public Optional<Mpa> getMpaById(Integer id) {
        return jdbcTemplate.query(SQL_SELECT_MPA_BY_ID, mpaRowMapper, id).stream().findFirst();
    }

    public List<Mpa> getAllMpa() {
        return jdbcTemplate.query(SQL_SELECT_MPA_ORDER_BY_ID, mpaRowMapper);
    }
}
