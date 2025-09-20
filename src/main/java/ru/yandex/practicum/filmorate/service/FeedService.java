package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedStorage feedStorage;

    public void addEvent(FeedEvent event) {
        // Добавь логирование для отладки
        log.info("Creating event: userId={}, eventType={}, operation={}, entityId={}",
                event.getUserId(), event.getEventType(), event.getOperation(), event.getEntityId());

        feedStorage.addEvent(event);
    }

    public List<FeedEvent> getUserFeed(int userId) {
        return feedStorage.getUserFeed(userId);
    }
}