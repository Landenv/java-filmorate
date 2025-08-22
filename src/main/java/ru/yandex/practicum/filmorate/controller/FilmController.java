package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final String DEFAULT_POPULAR_FILMS_COUNT = "10";
    private final FilmService filmService;
    private final FilmMapper filmMapper;

    @Autowired
    public FilmController(FilmService filmService, FilmMapper filmMapper) {
        this.filmService = filmService;
        this.filmMapper = filmMapper;
    }

    @PostMapping
    public Film create(@Valid @RequestBody FilmRequest filmRequest) {
        Film film = filmMapper.convertToFilm(filmRequest);
        log.info("Добавлен фильм: {}", film);
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody FilmRequest filmRequest) {
        if (filmRequest.getId() == null) {
            throw new ValidationException("ID фильма обязателен для обновления");
        }

        Film existingFilm = filmService.getById(filmRequest.getId());
        filmMapper.updateFilmFromRequest(existingFilm, filmRequest);

        log.info("Обновлен фильм: {}", existingFilm);
        return filmService.update(existingFilm);
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable int id) {
        return filmService.getById(id);
    }

    @GetMapping
    public List<Film> getAll() {
        return filmService.getAll();
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(
            @RequestParam(defaultValue = DEFAULT_POPULAR_FILMS_COUNT) int count) {
        return filmService.getPopularFilms(count);
    }

}