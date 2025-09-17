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
    private Integer eventId;        // уникальный ID события, может быть null при создании
    private Long timestamp;         // время события в миллисекундах, не null
    private Integer userId;         // ID пользователя, инициировавшего событие
    private EventType eventType;    // тип события: FRIEND, LIKE, REVIEW
    private Operation operation;    // операция: ADD, REMOVE, UPDATE
    private Integer entityId;       // ID сущности (пользователь, фильм или отзыв)

    public enum EventType {
        FRIEND, REVIEW, LIKE
    }

    public enum Operation {
        ADD, REMOVE, UPDATE
    }
}