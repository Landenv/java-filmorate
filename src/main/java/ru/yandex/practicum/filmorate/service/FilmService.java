package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        validateFilmData(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateFilmData(film);
        return filmStorage.update(film);
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

        if (filmStorage instanceof FilmDbStorage) {
            ((FilmDbStorage) filmStorage).addLike(filmId, userId);
        } else {
            film.getLikes().add(userId);
            filmStorage.update(film);
        }
    }

    public void removeLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException("Лайк не найден");
        }

        if (filmStorage instanceof FilmDbStorage) {
            ((FilmDbStorage) filmStorage).removeLike(filmId, userId);
        } else {
            film.getLikes().remove(userId);
            filmStorage.update(film);
        }
    }

    public List<Film> getPopularFilms(int count) {
        if (filmStorage instanceof FilmDbStorage) {
            return ((FilmDbStorage) filmStorage).getPopularFilms(count);
        } else {
            return filmStorage.getAll().stream()
                    .peek(film -> {
                        if (film.getLikes() == null) {
                            film.setLikes(new HashSet<>());
                        }
                    })
                    .sorted(Comparator.comparingInt(film -> -film.getLikes().size()))
                    .limit(count)
                    .toList();
        }
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

    // НОВЫЙ МЕТОД ДЛЯ УСТРАНЕНИЯ ДУБЛИРОВАНИЯ
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
}