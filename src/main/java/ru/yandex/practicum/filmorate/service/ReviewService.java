package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.enums.EventOperation;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.exception.ReviewCreateFailed;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exception.ReviewValidationException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.DbEventRepository;
import ru.yandex.practicum.filmorate.repository.DbReviewRepository;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final DbReviewRepository reviewRepository;
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final DbEventRepository eventRepository;

    public Review addReview(Review review) {
        log.info("Создание нового отзыва: filmId = {}, userId = {}, content = {}",
                review.getFilmId(), review.getUserId(), review.getContent());
        checkReviewWithoutIdOrThrow(review);
        Review newReview = reviewRepository.create(review)
                .orElseThrow(() -> new ReviewCreateFailed("Не удалось сохранить отзыв"));

        Event newEvent = Event.builder()
                .eventType(EventType.REVIEW)
                .operation(EventOperation.ADD)
                .userId(newReview.getUserId())
                .entityId(newReview.getReviewId())
                .build();
        eventRepository.addEvent(newEvent);

        log.info("Создан отзыв {}", newReview);
        return newReview;
    }

    public Review updateReview(Review review) {
        log.info("Изменение отзыва: reviewId = {}, content = {}, isPositive = {}",
                review.getReviewId(), review.getContent(), review.getIsPositive());
        checkReviewOrThrow(review);

        Review newReview = reviewRepository.update(review)
                .orElseThrow(() -> new ReviewCreateFailed("Не удалось изменить отзыв"));

        Event newEvent = Event.builder()
                .eventType(EventType.REVIEW)
                .operation(EventOperation.UPDATE)
                .userId(newReview.getUserId())
                .entityId(newReview.getReviewId())
                .build();
        eventRepository.addEvent(newEvent);

        log.info("Изменен отзыв {}", newReview);
        return newReview;
    }

    public Review getReviewById(Integer id) {
        log.info("getReviewById {}", id);
        return reviewRepository.getReviewById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Отзыв с id = " + id + " не найден"));
    }

    public boolean deleteReview(Integer id) {
        log.info("deleteReview {}", id);
        Review review = getReviewById(id);
        boolean res = reviewRepository.deleteReview(id);

        Event newEvent = Event.builder()
                .eventType(EventType.REVIEW)
                .operation(EventOperation.REMOVE)
                .userId(review.getUserId())
                .entityId(review.getReviewId())
                .build();
        eventRepository.addEvent(newEvent);

        return res;
    }

    public List<Review> getAll(Integer filmId, Integer limit) {
        log.info("getAll, filmId = {}, limit = {}", filmId, limit);
        if (filmId == null) {
            return reviewRepository.getAllWithLimit(limit);
        } else {
            if (!filmRepository.existsById(filmId)) {
                return new ArrayList<>();
            }
            return reviewRepository.getAllByFilmIdWithLimit(filmId, limit);
        }
    }

    public boolean addLike(Integer userId, Integer reviewId) {
        log.info("addLike, userId = {}, reviewId = {}", userId, reviewId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Юзер с id = " + userId + " не найден");
        }
        boolean isLike = reviewRepository.isLike(userId, reviewId);
        if (isLike) {
            throw new ReviewValidationException("Юзер " + userId + " уже поставил лайк отзыву " + reviewId);
        }
        boolean isDislike = reviewRepository.isDislike(userId, reviewId);
        Integer add = isDislike ? 2 : 1;
        log.info("addLike, isDislike = {}, isLike = {}, add = {}", isDislike, isLike, add);
        return reviewRepository.like(userId, reviewId, add);
    }

    public boolean addDislike(Integer userId, Integer reviewId) {
        log.info("addDislike, userId = {}, reviewId = {}", userId, reviewId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Юзер с id = " + userId + " не найден");
        }
        boolean isDislike = reviewRepository.isDislike(userId, reviewId);
        if (isDislike) {
            throw new ReviewValidationException("Юзер " + userId + " уже поставил дизлайк отзыву " + reviewId);
        }
        boolean isLike = reviewRepository.isLike(userId, reviewId);
        Integer deduct = isLike ? 2 : 1;
        log.info("addDislike, isLike = {}, isDislike = {}, deduct = {}", isLike, isDislike, deduct);
        return reviewRepository.dislike(userId, reviewId, deduct);
    }

    public boolean likeOff(Integer userId, Integer reviewId) {
        log.info("likeOff, userId = {}, reviewId = {}", userId, reviewId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Юзер с id = " + userId + " не найден");
        }
        if (!reviewRepository.isLike(userId, reviewId)) {
            throw new ReviewValidationException("Юзер " + userId + " не ставил лайк отзыву " + reviewId);
        }
        return reviewRepository.likeOff(userId, reviewId);
    }

    public boolean dislikeOff(Integer userId, Integer reviewId) {
        log.info("dislikeOff, userId = {}, reviewId = {}", userId, reviewId);
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
