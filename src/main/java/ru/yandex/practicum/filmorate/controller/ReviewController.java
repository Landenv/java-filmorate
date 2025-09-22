package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewResponse;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ReviewResponse create(@Valid @RequestBody ReviewRequest reviewRequest) {
        log.info("Создание отзыва: {}", reviewRequest);
        return reviewService.create(reviewRequest);
    }

    @PutMapping
    public ReviewResponse update(@Valid @RequestBody ReviewRequest reviewRequest) {
        log.info("Обновление отзыва: {}", reviewRequest);
        return reviewService.update(reviewRequest);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        log.info("Удаление отзыва с ID: {}", id);
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public ReviewResponse getById(@PathVariable int id) {
        return reviewService.getById(id);
    }

    @GetMapping
    public List<ReviewResponse> getReviews(
            @RequestParam(required = false) Integer filmId,
            @RequestParam(defaultValue = "10") int count) {
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Добавление лайка отзыву {} пользователем {}", id, userId);
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable int id, @PathVariable int userId) {
        log.info("Добавление дизлайка отзыву {} пользователем {}", id, userId);
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Удаление лайка отзыву {} пользователем {}", id, userId);
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable int id, @PathVariable int userId) {
        log.info("Удаление дизлайка отзыву {} пользователем {}", id, userId);
        reviewService.removeDislike(id, userId);
    }
}