package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class Review {
    private Integer reviewId;
    private String content;
    private Boolean isPositive;
    private Integer userId;
    private Integer filmId;
    private Integer useful;
    private LocalDateTime createdAt;

    @Builder.Default
    private Set<Integer> likes = new HashSet<>();

    @Builder.Default
    private Set<Integer> dislikes = new HashSet<>();
}