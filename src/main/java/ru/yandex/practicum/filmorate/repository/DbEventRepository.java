package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.repository.mappers.EventRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
@RequiredArgsConstructor
public class DbEventRepository {
    private static final String SQL_INSERT_EVENT =
            "INSERT INTO \"event\" (event_type, operation, user_id, entity_id, time_stamp) " +
                    "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_EVENTS_BY_USERID_DESC = "SELECT * FROM \"event\" WHERE user_id = ? " +
            "ORDER BY time_stamp DESC";
    private static final String SQL_SELECT_EVENTS_BY_USERID_ASC = "SELECT * FROM \"event\" WHERE user_id = ? " +
            "ORDER BY time_stamp ASC";

    private final JdbcTemplate jdbcTemplate;
    private final EventRowMapper eventRowMapper;

    public Optional<Event> addEvent(Event event) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        int rows = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_EVENT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, event.getEventType().toString());
            ps.setString(2, event.getOperation().toString());
            ps.setInt(3, event.getUserId());
            ps.setInt(4, event.getEntityId());
            ps.setLong(5, System.currentTimeMillis());
            return ps;
        }, keyHolder);

        if (rows == 0 || keyHolder.getKey() == null) {
            return Optional.empty();
        }
        event.setEventId(keyHolder.getKeyAs(Integer.class));

        return Optional.of(event);
    }

    public List<Event> findAllByUserId(Integer userId, String direct) {
        if (direct.equalsIgnoreCase("desc")) {
            return jdbcTemplate.query(SQL_SELECT_EVENTS_BY_USERID_DESC, eventRowMapper, userId);
        } else {
            return jdbcTemplate.query(SQL_SELECT_EVENTS_BY_USERID_ASC, eventRowMapper, userId);
        }
    }

}
