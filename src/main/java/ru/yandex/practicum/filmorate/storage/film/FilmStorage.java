package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Component
@Qualifier("filmDbStorage")
public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Film getById(int id);

    List<Film> getAll();

    void delete(int id);
}