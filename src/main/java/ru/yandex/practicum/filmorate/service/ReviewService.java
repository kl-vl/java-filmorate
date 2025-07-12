package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ReviewCreateFailed;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exception.ReviewValidationException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.DbReviewRepository;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final DbReviewRepository reviewRepository;
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    public Review addReview(Review review) {
        log.debug("Создание нового отзыва: filmId = {}, userId = {}, content = {}",
                review.getFilmId(), review.getUserId(), review.getContent());
        checkReviewWithoutIdOrThrow(review);
        Review newReview = reviewRepository.create(review)
                .orElseThrow(() -> new ReviewCreateFailed("Не удалось сохранить отзыв"));

        log.info("Отзыв создан, reviewId = {}", newReview.getReviewId());
        log.debug("Создан отзыв {}", newReview);
        return newReview;
    }

    public Review updateReview(Review review) {
        log.debug("Изменение отзыва: reviewId = {}, content = {}, isPositive = {}",
                review.getReviewId(), review.getContent(), review.getIsPositive());
        checkReviewOrThrow(review);

        Review newReview = reviewRepository.update(review)
                .orElseThrow(() -> new ReviewCreateFailed("Не удалось изменить отзыв"));

        log.info("Отзыв изменен, reviewId = {}", newReview.getReviewId());
        log.debug("Изменен отзыв {}", newReview);
        return newReview;
    }

    public Review getReviewById(Integer id) {
        log.debug("getReviewById {}", id);
        return reviewRepository.getReviewById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Отзыв с id = " + id + " не найден"));
    }

    public boolean deleteReview(Integer id) {
        log.debug("deleteReview {}", id);
        getReviewById(id);
        return reviewRepository.deleteReview(id);
    }

    public List<Review> getAll(Integer filmId, Integer limit) {
        log.debug("getAll, filmId = {}, limit = {}", filmId, limit);
        if (filmId == null) {
            return reviewRepository.getAllWithLimit(limit);
        } else {
            if (!filmRepository.existsById(filmId)) {
                throw new FilmNotFoundException("Фильм с id = " + filmId + " не найден");
            }
            return reviewRepository.getAllByFilmIdWithLimit(filmId, limit);
        }
    }

    public boolean addLike(Integer userId, Integer reviewId) {
        log.debug("addLike, userId = {}, reviewId = {}", userId, reviewId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Юзер с id = " + userId + " не найден");
        }
        boolean isLike = reviewRepository.isLike(userId, reviewId);
        if (isLike) {
            throw new ReviewValidationException("Юзер " + userId + " уже поставил лайк отзыву " + reviewId);
        }
        boolean isDislike = reviewRepository.isDislike(userId, reviewId);
        Integer add = isDislike ? 2 : 1;
        log.debug("addLike, isDislike = {}, isLike = {}, add = {}", isDislike, isLike, add);
        return reviewRepository.like(userId, reviewId, add);
    }

    public boolean addDislike(Integer userId, Integer reviewId) {
        log.debug("addDislike, userId = {}, reviewId = {}", userId, reviewId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Юзер с id = " + userId + " не найден");
        }
        boolean isDislike = reviewRepository.isDislike(userId, reviewId);
        if (isDislike) {
            throw new ReviewValidationException("Юзер " + userId + " уже поставил дизлайк отзыву " + reviewId);
        }
        boolean isLike = reviewRepository.isLike(userId, reviewId);
        Integer deduct = isLike ? 2 : 1;
        log.debug("addDislike, isLike = {}, isDislike = {}, deduct = {}", isLike, isDislike, deduct);
        return reviewRepository.dislike(userId, reviewId, deduct);
    }

    public boolean likeOff(Integer userId, Integer reviewId) {
        log.debug("likeOff, userId = {}, reviewId = {}", userId, reviewId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Юзер с id = " + userId + " не найден");
        }
        if (!reviewRepository.isLike(userId, reviewId)) {
            throw new ReviewValidationException("Юзер " + userId + " не ставил лайк отзыву " + reviewId);
        }
        return reviewRepository.likeOff(userId, reviewId);
    }

    public boolean dislikeOff(Integer userId, Integer reviewId) {
        log.debug("dislikeOff, userId = {}, reviewId = {}", userId, reviewId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Юзер с id = " + userId + " не найден");
        }
        if (!reviewRepository.isDislike(userId, reviewId)) {
            throw new ReviewValidationException("Юзер " + userId + " не ставил дизлайк отзыву " + reviewId);
        }
        return reviewRepository.dislikeOff(userId, reviewId);
    }

    private void checkReviewOrThrow(Review review) {
        if (review.getReviewId() == null) {
            throw new ReviewValidationException("Id должен быть указан");
        }
        checkReviewWithoutIdOrThrow(review);
    }

    private void checkReviewWithoutIdOrThrow(Review review) {
        if (review == null) {
            throw new IllegalStateException("Не передан объект отзыва");
        }
        if (review.getFilmId() == null) {
            throw new ReviewValidationException("id фильма == null");
        }
        if (review.getUserId() == null) {
            throw new ReviewValidationException("id юзера == null");
        }
        if (!filmRepository.existsById(review.getFilmId())) {
            throw new ReviewNotFoundException("Фильм с id = " + review.getFilmId() + " не найден");
        }
        if (!userRepository.existsById(review.getUserId())) {
            throw new ReviewNotFoundException("Юзер с id = " + review.getUserId() + " не найден");
        }
        if (review.getContent().isEmpty()) {
            throw new ReviewValidationException("Поле content не заполнено");
        }
        if (review.getIsPositive() == null) {
            throw new ReviewValidationException("Поле isPositive не заполнено");
        }

    }

}
