package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Integer reviewId;
    private String content;
    private Boolean isPositive;
    private Integer userId;
    private Integer filmId;
    private Integer useful;
    private LocalDateTime createdAt;
}