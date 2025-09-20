package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final String DEFAULT_POPULAR_FILMS_COUNT = "10";
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film create(@Valid @RequestBody FilmRequest filmRequest) {
        Film film = filmService.create(filmRequest);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody FilmRequest filmRequest) {
        if (filmRequest.getId() == null) {
            throw new ValidationException("ID фильма обязателен для обновления");
        }

        Film film = filmService.update(filmRequest);
        log.info("Обновлен фильм: {}", film);
        return film;
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable int id) {
        log.info("Удаление фильма с ID: {}", id);
        filmService.delete(id);
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
            @RequestParam(defaultValue = DEFAULT_POPULAR_FILMS_COUNT) int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getCommon(
            @RequestParam int userId,
            @RequestParam int friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getByDirectorId(@PathVariable int directorId,
                                      @RequestParam(defaultValue = "likes") String sortBy) {
        if (!"year".equals(sortBy) && !"likes".equals(sortBy)) {
            throw new ValidationException("sortBy must be 'year' or 'likes'");
        }
        return "year".equals(sortBy)
                ? filmService.getByDirectorOrderByYear(directorId)
                : filmService.getByDirectorOrderByLikes(directorId);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam String query,
                                  @RequestParam String by) {
        boolean byTitle = by.contains("title");
        boolean byDirector = by.contains("director");
        return filmService.searchFilms(query, byTitle, byDirector);
    }
}