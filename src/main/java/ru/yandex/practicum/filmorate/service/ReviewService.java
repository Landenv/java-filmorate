package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final ReviewMapper reviewMapper;

    public ReviewResponse create(ReviewRequest reviewRequest) {
        validateUserAndFilm(reviewRequest.getUserId(), reviewRequest.getFilmId());

        Review review = reviewMapper.convertToReview(reviewRequest);
        review.setUseful(0);

        Review createdReview = reviewStorage.create(review);
        return reviewMapper.convertToResponse(createdReview);
    }

    public ReviewResponse update(ReviewRequest reviewRequest) {
        if (reviewRequest.getReviewId() == null) {
            throw new ValidationException("ID отзыва обязателен для обновления");
        }

        Review existingReview = reviewStorage.getById(reviewRequest.getReviewId());
        validateUserAndFilm(reviewRequest.getUserId(), reviewRequest.getFilmId());

        reviewMapper.updateReviewFromRequest(existingReview, reviewRequest);
        Review updatedReview = reviewStorage.update(existingReview);

        return reviewMapper.convertToResponse(updatedReview);
    }

    public void delete(int id) {
        reviewStorage.delete(id);
    }

    public ReviewResponse getById(int id) {
        Review review = reviewStorage.getById(id);
        return reviewMapper.convertToResponse(review);
    }

    public List<ReviewResponse> getReviews(Integer filmId, int count) {
        List<Review> reviews;
        if (filmId != null) {
            filmStorage.getById(filmId);
            reviews = reviewStorage.getByFilmId(filmId, count);
        } else {
            reviews = reviewStorage.getAll(count);
        }

        return reviews.stream()
                .map(reviewMapper::convertToResponse)
                .collect(Collectors.toList());
    }

    public void addLike(int reviewId, int userId) {
        Review review = reviewStorage.getById(reviewId);
        userStorage.getById(userId);

        if (review.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь уже поставил лайк");
        }

        if (review.getDislikes().contains(userId)) {
            review.getDislikes().remove(userId);
            review.setUseful(review.getUseful() + 1);
        }

        review.getLikes().add(userId);
        review.setUseful(review.getUseful() + 1);
        reviewStorage.update(review);
    }

    public void addDislike(int reviewId, int userId) {
        Review review = reviewStorage.getById(reviewId);
        userStorage.getById(userId);

        if (review.getDislikes().contains(userId)) {
            throw new ValidationException("Пользователь уже поставил дизлайк");
        }

        if (review.getLikes().contains(userId)) {
            review.getLikes().remove(userId);
            review.setUseful(review.getUseful() - 1);
        }

        review.getDislikes().add(userId);
        review.setUseful(review.getUseful() - 1);
        reviewStorage.update(review);
    }

    public void removeLike(int reviewId, int userId) {
        Review review = reviewStorage.getById(reviewId);
        userStorage.getById(userId);

        if (!review.getLikes().contains(userId)) {
            throw new NotFoundException("Лайк не найден");
        }

        review.getLikes().remove(userId);
        review.setUseful(review.getUseful() - 1);
        reviewStorage.update(review);
    }

    public void removeDislike(int reviewId, int userId) {
        Review review = reviewStorage.getById(reviewId);
        userStorage.getById(userId);

        if (!review.getDislikes().contains(userId)) {
            throw new NotFoundException("Дизлайк не найден");
        }

        review.getDislikes().remove(userId);
        review.setUseful(review.getUseful() + 1);
        reviewStorage.update(review);
    }

    private void validateUserAndFilm(int userId, int filmId) {
        userStorage.getById(userId);
        filmStorage.getById(filmId);
    }
}