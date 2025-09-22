package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final UserDbStorage userStorage;
    private final FeedService feedService;

    @Transactional
    public void addFriend(int userId, int friendId) {
        log.info("addFriend({}, {}) - start", userId, friendId);

        userStorage.getById(userId);
        userStorage.getById(friendId);

        if (userStorage.getFriendIds(userId).contains(friendId)) {
            log.info("Friendship already exists: {} -> {}", userId, friendId);
            return;
        }

        userStorage.addFriend(userId, friendId);
        log.info("Friendship added: {} -> {}", userId, friendId);

        feedService.addEvent(FeedEvent.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(FeedEvent.EventType.FRIEND)
                .operation(FeedEvent.Operation.ADD)
                .entityId(friendId)
                .build());

        log.info("Feed event FRIEND ADD created for {} -> {}", userId, friendId);
    }

    @Transactional
    public void removeFriend(int userId, int friendId) {
        log.info("removeFriend({}, {}) - start", userId, friendId);

        userStorage.getById(userId);
        userStorage.getById(friendId);

        if (!userStorage.getFriendIds(userId).contains(friendId)) {
            log.info("Friendship doesn't exist: {} -> {}", userId, friendId);
            return;
        }

        userStorage.removeFriend(userId, friendId);
        log.info("Friendship removed: {} -> {}", userId, friendId);

        feedService.addEvent(FeedEvent.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(FeedEvent.EventType.FRIEND)
                .operation(FeedEvent.Operation.REMOVE)
                .entityId(friendId)
                .build());

        log.info("Feed event FRIEND REMOVE created for {} -> {}", userId, friendId);
    }

    public List<Integer> getFriendIds(int userId) {
        userStorage.getById(userId);
        return userStorage.getFriendIds(userId);
    }
}