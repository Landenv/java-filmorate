package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


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

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    // НОВАЯ ФУНКЦИОНАЛЬНОСТЬ: Работа с друзьями
    public void addFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public Collection<User> getFriends(int id) {
        User user = userStorage.getById(id);
        Set<User> friends = new HashSet<>();
        for (Integer friendId : user.getFriends()) {
            friends.add(userStorage.getById(friendId));
        }
        return friends;
    }

    public Collection<User> getCommonFriends(int id, int otherId) {
        User user = userStorage.getById(id);
        User otherUser = userStorage.getById(otherId);
        Set<User> commonFriends = new HashSet<>();

        for (Integer friendId : user.getFriends()) {
            if (otherUser.getFriends().contains(friendId)) {
                commonFriends.add(userStorage.getById(friendId));
            }
        }
        return commonFriends;
    }
}