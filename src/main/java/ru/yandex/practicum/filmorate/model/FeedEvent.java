package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedEvent {
    private Integer eventId;
    private Long timestamp;       // время события в миллисекундах
    private Integer userId;
    private EventType eventType;  // FRIEND, LIKE, REVIEW
    private Operation operation;  // ADD, REMOVE, UPDATE
    private Integer entityId;     // ID сущности (фильм, пользователь или отзыв)

    public enum EventType {
        LIKE, REVIEW, FRIEND
    }

    public enum Operation {
        ADD, REMOVE, UPDATE
    }
}