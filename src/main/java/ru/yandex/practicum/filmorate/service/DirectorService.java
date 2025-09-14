package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.director.DirectorMapper;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

@Service
public class DirectorService {
    private final DirectorStorage directorStorage;
    private final FilmStorage filmStorage;
    private DirectorMapper directorMapper;

}
