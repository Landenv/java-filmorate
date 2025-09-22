package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.ReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewResponse;
import ru.yandex.practicum.filmorate.model.Review;

import java.time.LocalDateTime;

@Component
public class ReviewMapper {

    public Review convertToReview(ReviewRequest reviewRequest) {
        return Review.builder()
                .reviewId(reviewRequest.getReviewId())
                .content(reviewRequest.getContent())
                .isPositive(reviewRequest.getIsPositive())
                .userId(reviewRequest.getUserId())
                .filmId(reviewRequest.getFilmId())
                .useful(reviewRequest.getUseful())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public ReviewResponse convertToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setReviewId(review.getReviewId());
        response.setContent(review.getContent());
        response.setIsPositive(review.getIsPositive());
        response.setUserId(review.getUserId());
        response.setFilmId(review.getFilmId());
        response.setUseful(review.getUseful());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }

    public void updateReviewFromRequest(Review review, ReviewRequest reviewRequest) {
        review.setContent(reviewRequest.getContent());
        review.setIsPositive(reviewRequest.getIsPositive());
    }
}