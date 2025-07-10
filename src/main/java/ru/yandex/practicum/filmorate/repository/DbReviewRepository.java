package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.mappers.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
@RequiredArgsConstructor
@Slf4j
public class DbReviewRepository {
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM \"review\" WHERE id = ?";
    private static final String SQL_INSERT_REVIEW = "INSERT INTO \"review\" (content, is_positive, user_id, film_id, useful) " +
            "VALUES (?, ?, ?, ?, 0)";
    private static final String SQL_UPDATE_REVIEW = "UPDATE \"review\" SET content = ?, is_positive = ?, user_id = ?, " +
            "film_id = ? WHERE id = ?"; // , useful = ?
    private static final String SQL_UPDATE_REVIEW_USEFUL = "UPDATE \"review\" SET useful = useful + ? WHERE id = ?";
    private static final String SQL_DELETE_REVIEW = "DELETE FROM \"review\" WHERE id = ?";

    private static final String SQL_SELECT_ALL_REVIEW_BY_FILM_WITH_LIMIT = "SELECT * FROM \"review\" WHERE film_id = ? " +
            "ORDER BY useful DESC LIMIT ?";
    private static final String SQL_SELECT_ALL_REVIEW_WITH_LIMIT = "SELECT * FROM \"review\" ORDER BY useful DESC LIMIT ?";

    private static final String SQL_MERGE_REVIEW_LIKES_SET_LIKE_DISLIKE = """
                MERGE INTO \"review_likes\" (user_id, review_id, is_like, is_dislike)
                KEY (user_id, review_id)
                VALUES (?, ?, ?, ?)
            """;
    private static final String SQL_MERGE_REVIEW_LIKES_OFF_LIKE = """
                MERGE INTO \"review_likes\" (user_id, review_id, is_like)
                KEY (user_id, review_id)
                VALUES (?, ?, false)
            """;
    private static final String SQL_MERGE_REVIEW_LIKES_OFF_DISLIKE = """
                MERGE INTO \"review_likes\" (user_id, review_id, is_dislike)
                KEY (user_id, review_id)
                VALUES (?, ?, false)
            """;
    private static final String SQL_SELECT_REVIEW_LIKES_LIKE = "SELECT is_like FROM \"review_likes\" " +
            "WHERE user_id = ? AND review_id = ?";

    private static final String SQL_SELECT_REVIEW_LIKES_DISLIKE = "SELECT is_dislike FROM \"review_likes\" " +
            "WHERE user_id = ? AND review_id = ?";


    private final JdbcTemplate jdbcTemplate;
    private final ReviewRowMapper reviewRowMapper;

    public Review create(Review review) {
        log.info("репозиторий - create");
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_REVIEW, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            review.setReviewId(keyHolder.getKeyAs(Integer.class));
            review.setUseful(0);
        } else {
            throw new RuntimeException("Не удалось сохранить данные");
        }
        return review;
    }

    public Review update(Review review) {
        log.info("репозиторий - update");
        Integer id = review.getReviewId();
        jdbcTemplate.update(
                SQL_UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                id
        );
        return getReviewById(id).get();
    }

    public Optional<Review> getReviewById(Integer id) {
        log.info("репозиторий - getReviewById {}", id);
        try {
            Review review = jdbcTemplate.queryForObject(SQL_SELECT_BY_ID, reviewRowMapper, id);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public void deleteReview(Integer id) {
        log.info("репозиторий - deleteReview {}", id);
        int rowsDeleted = jdbcTemplate.update(SQL_DELETE_REVIEW, id);
    }

    public List<Review> getAllWithLimit(Integer limit) {
        log.info("репозиторий - getAllWithLimit {}", limit);
        return jdbcTemplate.query(SQL_SELECT_ALL_REVIEW_WITH_LIMIT, reviewRowMapper, limit);
    }

    public List<Review> getAllByFilmIdWithLimit(Integer filmId, Integer limit) {
        log.info("репозиторий - getAllByFilmIdWithLimit filmId = {}, limit = {}", filmId, limit);
        return jdbcTemplate.query(SQL_SELECT_ALL_REVIEW_BY_FILM_WITH_LIMIT, reviewRowMapper, filmId, limit);
    }

    public void like(Integer userId, Integer reviewId, Integer add) {
        log.info("репозиторий - like userId = {}, reviewId = {}, add = {}", userId, reviewId, add);
        Integer i = jdbcTemplate.update(
                SQL_MERGE_REVIEW_LIKES_SET_LIKE_DISLIKE,
                userId,
                reviewId,
                true,
                false
        );
        log.info("репозиторий - like i = {}", i);
        jdbcTemplate.update(
                SQL_UPDATE_REVIEW_USEFUL,
                add,
                reviewId
        );
    }

    public void dislike(Integer userId, Integer reviewId, Integer deduct) {
        log.info("репозиторий - dislike userId = {}, reviewId = {}, deduct = {}", userId, reviewId, deduct);
        jdbcTemplate.update(
                SQL_MERGE_REVIEW_LIKES_SET_LIKE_DISLIKE,
                userId,
                reviewId,
                false,
                true
        );
        jdbcTemplate.update(
                SQL_UPDATE_REVIEW_USEFUL,
                -deduct,
                reviewId
        );
    }

    public void likeOff(Integer userId, Integer reviewId) {
        log.info("репозиторий - likeOff userId = {}, reviewId = {}", userId, reviewId);
        jdbcTemplate.update(
                SQL_MERGE_REVIEW_LIKES_OFF_LIKE,
                userId,
                reviewId
        );
        jdbcTemplate.update(
                SQL_UPDATE_REVIEW_USEFUL,
                -1,
                reviewId
        );
    }

    public void dislikeOff(Integer userId, Integer reviewId) {
        log.info("репозиторий - dislikeOff userId = {}, reviewId = {}", userId, reviewId);
        jdbcTemplate.update(
                SQL_MERGE_REVIEW_LIKES_OFF_DISLIKE,
                userId,
                reviewId
        );
        jdbcTemplate.update(
                SQL_UPDATE_REVIEW_USEFUL,
                1,
                reviewId
        );
    }

    public boolean isLike(Integer userId, Integer reviewId) {
        log.info("репозиторий - isLike userId = {}, reviewId = {}", userId, reviewId);
        try {
            boolean res = jdbcTemplate.queryForObject(
                    SQL_SELECT_REVIEW_LIKES_LIKE,
                    Boolean.class,
                    userId,
                    reviewId
            );
            return res;
        } catch (RuntimeException ignored) {
            return false;
        }

    }

    public boolean isDislike(Integer userId, Integer reviewId) {
        log.info("репозиторий - isDislike userId = {}, reviewId = {}", userId, reviewId);
        try {
            boolean res = jdbcTemplate.queryForObject(
                    SQL_SELECT_REVIEW_LIKES_DISLIKE,
                    Boolean.class,
                    userId,
                    reviewId
            );
            return res;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

}


