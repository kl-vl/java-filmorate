package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
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
        log.info("сервис - создание нового отзыва");
        checkReviewOrThrow(review);
        return reviewRepository.create(review);
    }

    public Review updateReview(Review review) {
        log.info("сервис - изменение существующего отзыва");
        checkReviewOrThrow(review);
        checkIdOrThrow(review);
        return reviewRepository.update(review);
    }

    public Review getReviewById(Integer id) {
        log.info("сервис - getReviewById {}", id);
        return reviewRepository.getReviewById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + id + " не найден"));
    }

    public void deleteReview(Integer id) {
        log.info("сервис - deleteReview {}", id);
        Review review = getReviewById(id);
        reviewRepository.deleteReview(id);
    }

    public List<Review> getAll(Integer filmId, Integer limit) {
        log.info("сервис - getAll, filmId = {}, limit = {}", filmId, limit);
        if (filmId == null) {
            return reviewRepository.getAllWithLimit(limit);
        } else {
            if (!filmRepository.existsById(filmId)) {
                throw new FilmNotFoundException("Фильм с id = " + filmId + " не найден");
            }
            return reviewRepository.getAllByFilmIdWithLimit(filmId, limit);
        }
    }

    public void addLike(Integer userId, Integer reviewId) {
        log.info("сервис - addLike, userId = {}, reviewId = {}", userId, reviewId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Юзер с id = " + userId + " не найден");
        }
        boolean isLike = reviewRepository.isLike(userId, reviewId);
        if (isLike) {
            throw new ValidationException("Юзер " + userId + " уже поставил лайк отзыву " + reviewId);
        }
        boolean isDislike = reviewRepository.isDislike(userId, reviewId);
        Integer add = isDislike ? 2 : 1;
        log.info("сервис - addLike, isDislike = {}, isLike = {}, add = {}", isDislike, isLike, add);
        reviewRepository.like(userId, reviewId, add);
    }

    public void addDislike(Integer userId, Integer reviewId) {
        log.info("сервис - addDislike, userId = {}, reviewId = {}", userId, reviewId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Юзер с id = " + userId + " не найден");
        }
        boolean isDislike = reviewRepository.isDislike(userId, reviewId);
        if (isDislike) {
            throw new ValidationException("Юзер " + userId + " уже поставил дизлайк отзыву " + reviewId);
        }
        boolean isLike = reviewRepository.isLike(userId, reviewId);
        Integer deduct = isLike ? 2 : 1;
        log.info("сервис - addDislike, isLike = {}, isDislike = {}, deduct = {}", isLike, isDislike, deduct);
        reviewRepository.dislike(userId, reviewId, deduct);
    }

    public void likeOff(Integer userId, Integer reviewId) {
        log.info("сервис - likeOff, userId = {}, reviewId = {}", userId, reviewId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Юзер с id = " + userId + " не найден");
        }
        if (!reviewRepository.isLike(userId, reviewId)) {
            throw new ValidationException("Юзер " + userId + " не ставил лайк отзыву " + reviewId);
        }
        reviewRepository.likeOff(userId, reviewId);
    }

    public void dislikeOff(Integer userId, Integer reviewId) {
        log.info("сервис - dislikeOff, userId = {}, reviewId = {}", userId, reviewId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Юзер с id = " + userId + " не найден");
        }
        if (!reviewRepository.isDislike(userId, reviewId)) {
            throw new ValidationException("Юзер " + userId + " не ставил дизлайк отзыву " + reviewId);
        }
        reviewRepository.dislikeOff(userId, reviewId);
    }

    private void checkIdOrThrow(Review review) {
        if (review.getReviewId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
    }

    private void checkReviewOrThrow(Review review) {
        if (review == null) {
            throw new IllegalStateException("Не передан объект отзыва");
        }
        if (review.getFilmId() == null) {
            throw new ValidationException("id фильма == null");
        }
        if (review.getUserId() == null) {
            throw new ValidationException("id юзера == null");
        }
        if (!filmRepository.existsById(review.getFilmId())) {
            throw new NotFoundException("Фильм с id = " + review.getFilmId() + " не найден");
        }
        if (!userRepository.existsById(review.getUserId())) {
            throw new NotFoundException("Юзер с id = " + review.getUserId() + " не найден");
        }
        if (review.getContent().isEmpty()) {
            throw new ValidationException("Поле content не заполнено");
        }
        if (review.getIsPositive() == null) {
            throw new ValidationException("Поле isPositive не заполнено");
        }

    }

}
