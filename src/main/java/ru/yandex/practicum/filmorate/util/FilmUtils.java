package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class FilmUtils {

    private FilmUtils() {

    }

    public static Set<Genre> getSortedGenres(Film film) {
        if (film.getGenres() == null) {
            return new LinkedHashSet<>();
        }
        return film.getGenres().stream()
                .sorted(Comparator.comparingInt(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static boolean hasGenres(Film film) {
        return film.getGenres() != null && !film.getGenres().isEmpty();
    }
}