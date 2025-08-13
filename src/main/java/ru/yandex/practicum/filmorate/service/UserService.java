package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public User getById(int id) {
        return userStorage.getById(id);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    // НОВАЯ ФУНКЦИОНАЛЬНОСТЬ: Работа с друзьями
    public void addFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        user.getFriendshipStatuses().put(friendId, FriendshipStatus.PENDING);
        friend.getFriendshipStatuses().put(userId, FriendshipStatus.PENDING);
    }

    public void removeFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        user.getFriendshipStatuses().remove(friendId);
        friend.getFriendshipStatuses().remove(userId);
    }

    // Новый метод для подтверждения дружбы
    public void confirmFriendship(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        user.getFriendshipStatuses().put(friendId, FriendshipStatus.CONFIRMED);
        friend.getFriendshipStatuses().put(userId, FriendshipStatus.CONFIRMED);
    }

    public List<User> getFriends(int id) {
        return userStorage.getById(id).getFriends().stream()
                .map(userStorage::getById)
                .collect(Collectors.toList()); // Изменено на toList()
    }

    public List<User> getCommonFriends(int id, int otherId) {
        User user = userStorage.getById(id);
        User otherUser = userStorage.getById(otherId);

        return user.getFriends().stream()
                .filter(friendId -> otherUser.getFriends().contains(friendId))
                .map(userStorage::getById)
                .collect(Collectors.toList()); // Изменено на toList()
    }

    // Новый метод для получения статуса дружбы
    public FriendshipStatus getFriendshipStatus(int userId, int friendId) {
        return userStorage.getById(userId)
                .getFriendshipStatuses()
                .getOrDefault(friendId, null);
    }
}