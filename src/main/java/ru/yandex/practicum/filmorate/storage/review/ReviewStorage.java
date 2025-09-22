package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    Review getById(int id);

    List<Review> getAll(int count);

    List<Review> getByFilmId(int filmId, int count);

    void delete(int id);

    void loadLikesAndDislikes(Review review);
}