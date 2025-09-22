package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    public void addEvent(FeedEvent event) {
        feedStorage.addEvent(event);
    }

    public List<FeedEvent> getUserFeed(int userId) {
        if (userStorage.getById(userId) == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        return feedStorage.getUserFeed(userId);
    }
}