package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final UserStorage userStorage;

    @GetMapping("/{id}/feed")
    public List<FeedEvent> getFeed(@PathVariable("id") int userId) {
        userStorage.getById(userId);
        return feedService.getUserFeed(userId);
    }
}