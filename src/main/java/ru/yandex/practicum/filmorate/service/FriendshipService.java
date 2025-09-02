package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final UserDbStorage userStorage;

    public void addFriend(int userId, int friendId) {
        log.info("FriendshipService.addFriend({}, {}) - started", userId, friendId);

        userStorage.getById(userId);
        userStorage.getById(friendId);

        log.info("Both users exist: {} and {}", userId, friendId);
        log.info("Calling UserDbStorage.addFriend({}, {})", userId, friendId);
        userStorage.addFriend(userId, friendId);

        log.info("FriendshipService.addFriend({}, {}) - completed", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        log.info("Removing friendship: {} -> {}", userId, friendId);

        userStorage.getById(userId);
        userStorage.getById(friendId);

        userStorage.removeFriend(userId, friendId);
        log.info("Friendship removal completed: {} -> {}", userId, friendId);
    }

    public List<Integer> getFriendIds(int userId) {
        userStorage.getById(userId);
        return userStorage.getFriendIds(userId);
    }
}