package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedStorage feedStorage;

    public void addEvent(FeedEvent event) {
        feedStorage.addEvent(event);
    }

    public List<FeedEvent> getUserFeed(int userId) {
        return feedStorage.getUserFeed(userId);
    }
}