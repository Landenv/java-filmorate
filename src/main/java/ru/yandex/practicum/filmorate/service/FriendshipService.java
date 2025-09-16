package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final UserDbStorage userStorage;
    private final FeedService feedService;

    public void addFriend(int userId, int friendId) {
        log.info("addFriend({}, {}) - start", userId, friendId);

        // Проверяем существование пользователей
        userStorage.getById(userId);
        userStorage.getById(friendId);

        // Добавляем дружбу
        userStorage.addFriend(userId, friendId);
        log.info("Friendship added: {} -> {}", userId, friendId);

        // Создаём событие в ленте
        feedService.addEvent(FeedEvent.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(FeedEvent.EventType.FRIEND)
                .operation(FeedEvent.Operation.ADD)
                .entityId(friendId)
                .build());
        log.info("Feed event added for FRIEND ADD: {} -> {}", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        log.info("removeFriend({}, {}) - start", userId, friendId);

        // Проверяем существование пользователей
        userStorage.getById(userId);
        userStorage.getById(friendId);

        // Удаляем дружбу
        userStorage.removeFriend(userId, friendId);
        log.info("Friendship removed: {} -> {}", userId, friendId);

        // Создаём событие в ленте
        feedService.addEvent(FeedEvent.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(FeedEvent.EventType.FRIEND)
                .operation(FeedEvent.Operation.REMOVE)
                .entityId(friendId)
                .build());
        log.info("Feed event added for FRIEND REMOVE: {} -> {}", userId, friendId);
    }

    public List<Integer> getFriendIds(int userId) {
        userStorage.getById(userId);
        return userStorage.getFriendIds(userId);
    }
}