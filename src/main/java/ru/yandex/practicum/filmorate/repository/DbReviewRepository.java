package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
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
public class DbReviewRepository {
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM \"review\" WHERE id = ?";
    private static final String SQL_INSERT_REVIEW = "INSERT INTO \"review\" (content, is_positive, user_id, film_id, useful) " +
            "VALUES (?, ?, ?, ?, 0)";
    private static final String SQL_UPDATE_REVIEW = "UPDATE \"review\" SET content = ?, is_positive = ? WHERE id = ?";
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

    public Optional<Review> create(Review review) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        int rows = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_REVIEW, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            return ps;
        }, keyHolder);

        if (rows == 0 || keyHolder.getKey() == null) {
            return Optional.empty();
        }
        review.setReviewId(keyHolder.getKeyAs(Integer.class));
        review.setUseful(0);

        return Optional.of(review);
    }

    public Optional<Review> update(Review review) {
        Integer id = review.getReviewId();
        int rows = jdbcTemplate.update(
                SQL_UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                id
        );
        Optional<Review> optReview = getReviewById(id);
        return optReview.isPresent() ? optReview : Optional.empty();
    }

    public Optional<Review> getReviewById(Integer id) {
        try {
            Review review = jdbcTemplate.queryForObject(SQL_SELECT_BY_ID, reviewRowMapper, id);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public boolean deleteReview(Integer id) {
        int rows = jdbcTemplate.update(SQL_DELETE_REVIEW, id);
        return rows > 0;
    }

    public List<Review> getAllWithLimit(Integer limit) {
        return jdbcTemplate.query(SQL_SELECT_ALL_REVIEW_WITH_LIMIT, reviewRowMapper, limit);
    }

    public List<Review> getAllByFilmIdWithLimit(Integer filmId, Integer limit) {
        return jdbcTemplate.query(SQL_SELECT_ALL_REVIEW_BY_FILM_WITH_LIMIT, reviewRowMapper, filmId, limit);
    }

    public boolean like(Integer userId, Integer reviewId, Integer add) {
        jdbcTemplate.update(
                SQL_MERGE_REVIEW_LIKES_SET_LIKE_DISLIKE,
                userId,
                reviewId,
                true,
                false
        );
        int rows = jdbcTemplate.update(
                SQL_UPDATE_REVIEW_USEFUL,
                add,
                reviewId
        );
        return rows > 0;
    }

    public boolean dislike(Integer userId, Integer reviewId, Integer deduct) {
        jdbcTemplate.update(
                SQL_MERGE_REVIEW_LIKES_SET_LIKE_DISLIKE,
                userId,
                reviewId,
                false,
                true
        );
        int rows = jdbcTemplate.update(
                SQL_UPDATE_REVIEW_USEFUL,
                -deduct,
                reviewId
        );
        return rows > 0;
    }

    public boolean likeOff(Integer userId, Integer reviewId) {
        jdbcTemplate.update(
                SQL_MERGE_REVIEW_LIKES_OFF_LIKE,
                userId,
                reviewId
        );
        int rows = jdbcTemplate.update(
                SQL_UPDATE_REVIEW_USEFUL,
                -1,
                reviewId
        );
        return rows > 0;
    }

    public boolean dislikeOff(Integer userId, Integer reviewId) {
        jdbcTemplate.update(
                SQL_MERGE_REVIEW_LIKES_OFF_DISLIKE,
                userId,
                reviewId
        );
        int rows = jdbcTemplate.update(
                SQL_UPDATE_REVIEW_USEFUL,
                1,
                reviewId
        );
        return rows > 0;
    }

    public boolean isLike(Integer userId, Integer reviewId) {
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


