package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmDbStorage;
    private final UserStorage userDbStorage;
    private final FilmMapper filmMapper;
    private final DirectorDbStorage directorDbStorage;
    private final FeedService feedService;

    public Film create(FilmRequest filmRequest) {
        log.info("Creating film");
        Film film = filmMapper.convertToFilm(filmRequest);
        validateFilmData(film);
        return filmDbStorage.create(film);
    }

    public Film update(FilmRequest filmRequest) {
        log.info("Updating film {}", filmRequest.getId());
        if (filmRequest.getId() == null) throw new ValidationException("ID фильма обязателен для обновления");

        Film existing = filmDbStorage.getById(filmRequest.getId());
        filmMapper.updateFilmFromRequest(existing, filmRequest);
        validateFilmData(existing);
        return filmDbStorage.update(existing);
    }

    public void delete(int id) {
        log.info("Deleting film {}", id);
        filmDbStorage.delete(id);
    }

    public Film getById(int id) {
        return filmDbStorage.getById(id);
    }

    public List<Film> getAll() {
        return filmDbStorage.getAll();
    }

    public void addLike(int filmId, int userId) {
        log.info("User {} likes film {}", userId, filmId);
        Film film = filmDbStorage.getById(filmId);
        userDbStorage.getById(userId);

        if (film.getLikes().contains(userId)) throw new ValidationException("Пользователь уже поставил лайк");

        filmDbStorage.addLike(filmId, userId);

        feedService.addEvent(FeedEvent.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(FeedEvent.EventType.LIKE)
                .operation(FeedEvent.Operation.ADD)
                .entityId(filmId)
                .build());
    }

    public void removeLike(int filmId, int userId) {
        log.info("User {} removes like from film {}", userId, filmId);
        Film film = filmDbStorage.getById(filmId);
        userDbStorage.getById(userId);

        if (!film.getLikes().contains(userId)) throw new NotFoundException("Лайк не найден");

        filmDbStorage.removeLike(filmId, userId);

        feedService.addEvent(FeedEvent.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(FeedEvent.EventType.LIKE)
                .operation(FeedEvent.Operation.REMOVE)
                .entityId(filmId)
                .build());
    }

    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        return filmDbStorage.getPopularFilms(count, genreId, year);
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        return filmDbStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getRecommendedFilms(int id) {
        return filmDbStorage.getRecommendedFilms(id);
    }

    public void addGenre(int filmId, Genre genre) {
        log.info("Adding genre {} to film {}", genre.getId(), filmId);
        Film film = filmDbStorage.getById(filmId);
        film.getGenres().add(genre);
        filmDbStorage.update(film);
    }

    public void removeGenre(int filmId, Genre genre) {
        log.info("Removing genre {} from film {}", genre.getId(), filmId);
        Film film = filmDbStorage.getById(filmId);
        film.getGenres().remove(genre);
        filmDbStorage.update(film);
    }

    private void validateFilmData(Film film) {
        if (film.getMpa() == null) throw new ValidationException("Рейтинг MPA обязателен");
        validateMpa(film.getMpa().getId());

        Set<Integer> genreIds = extractGenreIds(film);
        validateGenres(genreIds);
    }

    private void validateMpa(int mpaId) {
        if (mpaId < 1 || mpaId > 5) throw new NotFoundException("Рейтинг MPA с ID " + mpaId + " не найден");
    }

    private void validateGenres(Set<Integer> genreIds) {
        if (genreIds != null) {
            for (Integer genreId : genreIds) {
                if (genreId < 1 || genreId > 6)
                    throw new NotFoundException("Жанр с ID " + genreId + " не найден");
            }
        }
    }

    private Set<Integer> extractGenreIds(Film film) {
        if (film.getGenres() == null) return new HashSet<>();
        return film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
    }

    public List<Film> getByDirectorOrderByYear(int directorId) {
        validateDirector(directorId);
        return filmDbStorage.getFilmsDerectorByDate(directorId);
    }

    public List<Film> getByDirectorOrderByLikes(int directorId) {
        validateDirector(directorId);
        return filmDbStorage.getFilmsDerectorByLike(directorId);
    }

    private void validateDirector(int directorId) {
        if (directorId <= 0) {
            throw new IllegalArgumentException("directorId должен быть > 0");
        }
        Director d = directorDbStorage.getDirectorsById(directorId);
        if (d == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    "Режиссёр " + directorId + " не найден");
        }
    }
}