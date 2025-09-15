package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    private static final String GET_FILM_BY_ID_SQL = """
            SELECT f.*, m.mpa_name, m.description as mpa_description
            FROM films f
            JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
            WHERE f.film_id = ?""";

    private static final String GET_ALL_FILMS_SQL = """
            SELECT f.*, m.mpa_name, m.description as mpa_description
            FROM films f
            JOIN mpa_ratings m ON f.mpa_id = m.mpa_id""";

    private static final String GET_POPULAR_FILMS_SQL = """
            SELECT f.*, m.mpa_name, m.description as mpa_description,
                   COUNT(l.user_id) as likes_count
            FROM films f
            JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
            LEFT JOIN likes l ON f.film_id = l.film_id
            GROUP BY f.film_id
            ORDER BY likes_count DESC
            LIMIT ?""";

    private static final String GET_COMMON_FILMS_SQL = """
            SELECT f.*, m.mpa_name, m.description as mpa_description
            FROM films f
            JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
            JOIN likes l1 ON f.film_id = l1.film_id AND l1.user_id = ?
            JOIN likes l2 ON f.film_id = l2.film_id AND l2.user_id = ?""";

    @Override
    public Film create(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("film_name", film.getName());
        parameters.put("film_description", film.getDescription());
        parameters.put("film_release_date", film.getReleaseDate());
        parameters.put("film_duration", film.getDuration());
        parameters.put("mpa_id", film.getMpa().getId());

        Number generatedId = simpleJdbcInsert.executeAndReturnKey(parameters);
        film.setId(generatedId.intValue());

        saveGenres(film);
        return getById(film.getId());
    }

    @Override
    public Film update(Film film) {
        log.info("Updating film with ID: {}", film.getId());

        String sql = "UPDATE films SET film_name = ?, film_description = ?, film_release_date = ?, " +
                "film_duration = ?, mpa_id = ? WHERE film_id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        log.info("Rows updated: {}", rowsUpdated);

        if (rowsUpdated == 0) {
            log.error("Film with ID {} not found for update", film.getId());
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        updateGenres(film);
        Film updatedFilm = getById(film.getId());
        log.info("Film updated successfully: {}", updatedFilm);

        return updatedFilm;
    }

    @Override
    public Film getById(int id) {
        Film film = jdbcTemplate.query(GET_FILM_BY_ID_SQL, filmRowMapper, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));

        loadLikes(film);
        loadGenres(film);
        return film;
    }

    @Override
    public List<Film> getAll() {
        List<Film> films = jdbcTemplate.query(GET_ALL_FILMS_SQL, filmRowMapper);
        enrichFilmsWithLikesAndGenres(films);
        return films;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM films WHERE film_id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, id);

        if (rowsDeleted == 0) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
    }

    private void loadLikes(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        Set<Integer> likes = new HashSet<>(jdbcTemplate.query(sql,
                (resultSet, rowNum) -> resultSet.getInt("user_id"),
                film.getId()));
        film.setLikes(likes);
    }

    private void loadGenres(Film film) {
        String sql = "SELECT g.genre_id, g.genre_name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.genre_id";

        List<Genre> genres = jdbcTemplate.query(sql, (resultSet, rowNum) ->
                new Genre(
                        resultSet.getInt("genre_id"),
                        resultSet.getString("genre_name")
                ), film.getId());

        film.setGenres(new LinkedHashSet<>(genres));
    }

    private void saveGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            film.getGenres().forEach(genre ->
                    jdbcTemplate.update(sql, film.getId(), genre.getId()));
        }
    }

    private void updateGenres(Film film) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            film.getGenres().forEach(genre ->
                    jdbcTemplate.update(insertSql, film.getId(), genre.getId()));
        }
    }

    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> films = jdbcTemplate.query(GET_POPULAR_FILMS_SQL, filmRowMapper, count);
        enrichFilmsWithLikesAndGenres(films);
        return films;
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        List<Film> films = jdbcTemplate.query(GET_COMMON_FILMS_SQL, filmRowMapper, userId, friendId);
        enrichFilmsWithLikesAndGenres(films);
        return films;
    }

    // Новые методы для batch-загрузки
    private Map<Integer, Set<Integer>> loadLikesForFilms(List<Integer> filmIds) {
        if (filmIds.isEmpty()) {
            return Map.of();
        }

        String sql = """
                SELECT film_id, user_id
                FROM likes
                WHERE film_id IN (%s)
                """.formatted(filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(",")));

        Map<Integer, Set<Integer>> likesByFilm = new HashMap<>();

        jdbcTemplate.query(sql, ps -> {
            for (int i = 0; i < filmIds.size(); i++) {
                ps.setInt(i + 1, filmIds.get(i));
            }
        }, rs -> {
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                int userId = rs.getInt("user_id");
                likesByFilm
                        .computeIfAbsent(filmId, k -> new HashSet<>())
                        .add(userId);
            }
        });

        return likesByFilm;
    }

    private Map<Integer, Set<Genre>> loadGenresForFilms(List<Integer> filmIds) {
        if (filmIds.isEmpty()) {
            return Map.of();
        }

        String sql = """
                SELECT fg.film_id, g.genre_id, g.genre_name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.genre_id
                WHERE fg.film_id IN (%s)
                ORDER BY g.genre_id
                """.formatted(filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(",")));

        Map<Integer, Set<Genre>> genresByFilm = new HashMap<>();

        jdbcTemplate.query(sql, ps -> {
            for (int i = 0; i < filmIds.size(); i++) {
                ps.setInt(i + 1, filmIds.get(i));
            }
        }, rs -> {
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                genresByFilm
                        .computeIfAbsent(filmId, k -> new LinkedHashSet<>())
                        .add(new Genre(rs.getInt("genre_id"), rs.getString("genre_name")));
            }
        });

        return genresByFilm;
    }

    private void enrichFilmsWithLikesAndGenres(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Integer, Set<Integer>> likesByFilm = loadLikesForFilms(filmIds);
        Map<Integer, Set<Genre>> genresByFilm = loadGenresForFilms(filmIds);

        films.forEach(film -> {
            film.setLikes(likesByFilm.getOrDefault(film.getId(), new HashSet<>()));
            film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));
        });
    }
}