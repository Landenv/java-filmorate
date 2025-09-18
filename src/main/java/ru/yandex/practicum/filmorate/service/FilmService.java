package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
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

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FilmMapper filmMapper;
    private final DirectorDbStorage directorDbStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       FilmMapper filmMapper, DirectorDbStorage directorDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.filmMapper = filmMapper;
        this.directorDbStorage = directorDbStorage;
    }

    public Film create(FilmRequest filmRequest) {
        Film film = filmMapper.convertToFilm(filmRequest);
        validateFilmData(film);
        return filmStorage.create(film);
    }

    public Film update(FilmRequest filmRequest) {
        if (filmRequest.getId() == null) {
            throw new ValidationException("ID фильма обязателен для обновления");
        }

        Film existingFilm = filmStorage.getById(filmRequest.getId());
        filmMapper.updateFilmFromRequest(existingFilm, filmRequest);
        validateFilmData(existingFilm);
        return filmStorage.update(existingFilm);
    }

    public Film getById(int id) {
        return filmStorage.getById(id);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь уже поставил лайк");
        }

        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException("Лайк не найден");
        }

        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    public void addGenre(int filmId, Genre genre) {
        Film film = filmStorage.getById(filmId);
        film.getGenres().add(genre);
        filmStorage.update(film);
    }

    public void removeGenre(int filmId, Genre genre) {
        Film film = filmStorage.getById(filmId);
        film.getGenres().remove(genre);
        filmStorage.update(film);
    }

    private void validateFilmData(Film film) {
        if (film.getMpa() == null) {
            throw new ValidationException("Рейтинг MPA обязателен");
        }

        int mpaId = film.getMpa().getId();
        validateMpa(mpaId);

        Set<Integer> genreIds = extractGenreIds(film);
        validateGenres(genreIds);
    }

    private void validateMpa(int mpaId) {
        if (mpaId < 1 || mpaId > 5) {
            throw new NotFoundException("Рейтинг MPA с ID " + mpaId + " не найден");
        }
    }

    private void validateGenres(Set<Integer> genreIds) {
        if (genreIds != null) {
            for (Integer genreId : genreIds) {
                if (genreId < 1 || genreId > 6) {
                    throw new NotFoundException("Жанр с ID " + genreId + " не найден");
                }
            }
        }
    }

    private Set<Integer> extractGenreIds(Film film) {
        if (film.getGenres() == null) {
            return new HashSet<>();
        }
        return film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
    }

    public List<Film> getByDirectorOrderByYear(int directorId) {
        validateDirector(directorId);
        return filmStorage.getFilmsDerectorByDate(directorId);
    }

    public List<Film> getByDirectorOrderByLikes(int directorId) {
        validateDirector(directorId);
        return filmStorage.getFilmsDerectorByLike(directorId);
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