package ru.yandex.practicum.filmorate.storage.film;


import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Component
public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Film getById(int id);

    List<Film> getAll();

    void delete(int id);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> getPopularFilms(int count, Integer genreId, Integer year);

    List<Film> getCommonFilms(int userId, int friendId);

    List<Film> getRecommendedFilms(int id);

    List<Film> getFilmsDerectorByDate(int id);

    List<Film> getFilmsDerectorByLike(int id);
}