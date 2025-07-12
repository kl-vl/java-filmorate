package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review getById(@PathVariable Integer id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public Collection<Review> getAll(
            @RequestParam(name = "count", required = false, defaultValue = "10") Integer count,
            @RequestParam(name = "filmId", required = false) Integer filmId
    ) {
        return reviewService.getAll(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.addLike(userId, id);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislike(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.addDislike(userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void likeOff(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.likeOff(userId, id);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void dislikeOff(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.dislikeOff(userId, id);
    }

}
