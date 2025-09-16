package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final ReviewMapper reviewMapper;
    private final FeedService feedService;

    public ReviewResponse create(ReviewRequest reviewRequest) {
        log.info("create review by user {}", reviewRequest.getUserId());
        userStorage.getById(reviewRequest.getUserId());
        filmStorage.getById(reviewRequest.getFilmId());

        Review review = reviewMapper.convertToReview(reviewRequest);
        review.setUseful(0);
        Review created = reviewStorage.create(review);

        // Feed событие REVIEW ADD
        feedService.addEvent(FeedEvent.builder()
                .timestamp(System.currentTimeMillis())
                .userId(created.getUserId())
                .eventType(FeedEvent.EventType.REVIEW)
                .operation(FeedEvent.Operation.ADD)
                .entityId(created.getReviewId())
                .build());

        log.info("Review created with ID {}", created.getReviewId());
        return reviewMapper.convertToResponse(created);
    }

    public ReviewResponse update(ReviewRequest reviewRequest) {
        log.info("update review {}", reviewRequest.getReviewId());
        if (reviewRequest.getReviewId() == null) throw new ValidationException("ID обязателен");

        Review existing = reviewStorage.getById(reviewRequest.getReviewId());
        userStorage.getById(reviewRequest.getUserId());
        filmStorage.getById(reviewRequest.getFilmId());

        reviewMapper.updateReviewFromRequest(existing, reviewRequest);
        Review updated = reviewStorage.update(existing);

        // Feed событие REVIEW UPDATE
        feedService.addEvent(FeedEvent.builder()
                .timestamp(System.currentTimeMillis())
                .userId(updated.getUserId())
                .eventType(FeedEvent.EventType.REVIEW)
                .operation(FeedEvent.Operation.UPDATE)
                .entityId(updated.getReviewId())
                .build());

        log.info("Review updated with ID {}", updated.getReviewId());
        return reviewMapper.convertToResponse(updated);
    }

    public void delete(int id) {
        log.info("delete review {}", id);
        reviewStorage.delete(id);
    }

    public ReviewResponse getById(int id) {
        Review review = reviewStorage.getById(id);
        return reviewMapper.convertToResponse(review);
    }

    public List<ReviewResponse> getReviews(Integer filmId, int count) {
        List<Review> reviews = (filmId != null) ?
                reviewStorage.getByFilmId(filmId, count) :
                reviewStorage.getAll(count);

        return reviews.stream()
                .map(reviewMapper::convertToResponse)
                .collect(Collectors.toList());
    }

    public void addLike(int reviewId, int userId) {
        log.info("add like to review {} by user {}", reviewId, userId);
        Review review = reviewStorage.getById(reviewId);
        userStorage.getById(userId);

        if (review.getLikes().contains(userId)) throw new ValidationException("Уже лайкнул");
        if (review.getDislikes().remove(userId)) review.setUseful(review.getUseful() + 1);

        review.getLikes().add(userId);
        review.setUseful(review.getUseful() + 1);
        reviewStorage.update(review);

        feedService.addEvent(FeedEvent.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(FeedEvent.EventType.LIKE)
                .operation(FeedEvent.Operation.ADD)
                .entityId(reviewId)
                .build());
    }

    public void addDislike(int reviewId, int userId) {
        log.info("add dislike to review {} by user {}", reviewId, userId);
        Review review = reviewStorage.getById(reviewId);
        userStorage.getById(userId);

        if (review.getDislikes().contains(userId)) throw new ValidationException("Уже дизлайкнул");
        if (review.getLikes().remove(userId)) review.setUseful(review.getUseful() - 1);

        review.getDislikes().add(userId);
        review.setUseful(review.getUseful() - 1);
        reviewStorage.update(review);

        feedService.addEvent(FeedEvent.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(FeedEvent.EventType.LIKE)
                .operation(FeedEvent.Operation.REMOVE)
                .entityId(reviewId)
                .build());
    }

    public void removeLike(int reviewId, int userId) {
        log.info("remove like from review {} by user {}", reviewId, userId);
        Review review = reviewStorage.getById(reviewId);
        userStorage.getById(userId);

        if (!review.getLikes().contains(userId)) throw new NotFoundException("Лайк не найден");

        review.getLikes().remove(userId);
        review.setUseful(review.getUseful() - 1);
        reviewStorage.update(review);

        feedService.addEvent(FeedEvent.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(FeedEvent.EventType.LIKE)
                .operation(FeedEvent.Operation.REMOVE)
                .entityId(reviewId)
                .build());
    }

    public void removeDislike(int reviewId, int userId) {
        log.info("remove dislike from review {} by user {}", reviewId, userId);
        Review review = reviewStorage.getById(reviewId);
        userStorage.getById(userId);

        if (!review.getDislikes().contains(userId)) throw new NotFoundException("Дизлайк не найден");

        review.getDislikes().remove(userId);
        review.setUseful(review.getUseful() + 1);
        reviewStorage.update(review);

        feedService.addEvent(FeedEvent.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(FeedEvent.EventType.LIKE)
                .operation(FeedEvent.Operation.ADD)
                .entityId(reviewId)
                .build());
    }
}