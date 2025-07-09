package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.mappers.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Primary
@Repository
@RequiredArgsConstructor
public class DbUserRepository implements UserRepository {

    private static final String SQL_INSERT_USER = "INSERT INTO \"user\" (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String SQL_UPDATE_USER = "UPDATE \"user\" SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT id, email, login, name, birthday FROM \"user\"";
    private static final String SQL_SELECT_BY_ID = "SELECT id, email, login, name, birthday FROM \"user\" WHERE id = ?";
    private static final String SQL_EXISTS_BY_ID = "SELECT COUNT(*) FROM \"user\" WHERE id = ?";
    private static final String SQL_INSERT_FRIENDSHIP = "INSERT INTO \"friendship\" (user_id, friend_id, accepted) VALUES (?, ?, true)";
    private static final String SQL_DELETE_FRIENDSHIP = "DELETE FROM \"friendship\" WHERE user_id = ? AND friend_id = ?";
    private static final String SQL_SELECT_FRIENDSHIP_BY_USER_ID = "SELECT friend_id FROM \"friendship\" WHERE user_id = ?";
    private static final String SQL_SELECT_FRIENDSHIP_COMMON = """
            SELECT f1.friend_id
            FROM "friendship" f1
            JOIN "friendship" f2 ON f1.friend_id = f2.friend_id
            WHERE f1.user_id = ? AND f2.user_id = ?
            """;
    private static final String SQL_SELECT_FRIENDSHIP_BY_IDS = "SELECT COUNT(*) FROM \"friendship\" WHERE user_id = ? AND friend_id = ?";
    private static final String SQL_REMOVE_USER_BY_ID = "DELETE FROM \"user\" WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    @Transactional
    public Optional<User> create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return Optional.of(user);
    }

    @Override
    @Transactional
    public Optional<User> update(User user) {
        int updated = jdbcTemplate.update(SQL_UPDATE_USER,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );

        return updated == 1 ? Optional.of(user) : Optional.empty();
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query(SQL_SELECT_ALL, userRowMapper);
    }

    @Override
    public boolean existsById(Integer id) {
        Integer count = jdbcTemplate.queryForObject(SQL_EXISTS_BY_ID, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Optional<User> getById(Integer id) {
        List<User> users = jdbcTemplate.query(SQL_SELECT_BY_ID, userRowMapper, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.getFirst());
    }

    @Override
    @Transactional
    public boolean addFriend(Integer userId, Integer friendId) {
        return jdbcTemplate.update(SQL_INSERT_FRIENDSHIP, userId, friendId) > 0;
    }

    @Override
    @Transactional
    public boolean removeFriend(Integer userId, Integer friendId) {
        return jdbcTemplate.update(SQL_DELETE_FRIENDSHIP, userId, friendId) > 0;
    }

    @Override
    public List<Integer> getFriends(Integer userId) {
        return jdbcTemplate.queryForList(SQL_SELECT_FRIENDSHIP_BY_USER_ID, Integer.class, userId);
    }

    @Override
    public List<Integer> getCommonFriends(Integer userId1, Integer userId2) {
        return jdbcTemplate.queryForList(SQL_SELECT_FRIENDSHIP_COMMON, Integer.class, userId1, userId2);
    }

    @Override
    public boolean areFriends(Integer userId, Integer friendId) {
        Integer count = jdbcTemplate.queryForObject(SQL_SELECT_FRIENDSHIP_BY_IDS, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    @Override
    public Optional<User> get(Integer id) {
        return getById(id);
    }

    @Override
    public boolean removeUserById(Integer userId) {
        int delete = jdbcTemplate.update(SQL_REMOVE_USER_BY_ID, userId);

        return delete > 0;
    }

}