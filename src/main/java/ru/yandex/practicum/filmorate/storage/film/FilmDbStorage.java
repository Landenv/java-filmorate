package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
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

    private static final String GET_RECOMMENDED_FILMS_SQL = """
            SELECT f.*, m.mpa_name, m.description as mpa_description
            FROM films f
            JOIN likes l ON f.film_id = l.film_id
            JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
            WHERE l.user_id IN (SELECT l2.user_id
            	                FROM likes l1
            	                JOIN likes l2 ON l1.film_id = l2.film_id AND l1.user_id != l2.user_id
            	                WHERE l1.user_id = ?
            	                GROUP BY l2.user_id
            	                ORDER BY COUNT(*) DESC)
            AND l.film_id NOT IN (SELECT film_id
            	                  FROM likes
            	                  WHERE user_id = ?)""";

    private static final String DELETE_DIRECTORS =
            "DELETE FROM film_directors WHERE film_id = ?";
    private static final String INSERT_DIRECTOR =
            "INSERT INTO film_directors(film_id, director_id) VALUES (?, ?)";

    // по дате выпуска
    private static final String GET_BY_DIRECTOR_ORDER_BY_DATE = """
               SELECT f.*, m.mpa_name, m.description AS mpa_description
               FROM films f
               JOIN film_directors fd ON fd.film_id = f.film_id
               JOIN mpa_ratings m ON m.mpa_id = f.mpa_id
               WHERE fd.director_id = ?
               ORDER BY f.film_release_date ASC
            """;

    // по количеству лайков
    private static final String GET_BY_DIRECTOR_ORDER_BY_LIKES = """
                    SELECT f.*, m.mpa_name, m.description AS mpa_description, COALESCE(l.cnt, 0) AS likes_count
                FROM films f
                JOIN film_directors fd ON fd.film_id = f.film_id
                JOIN mpa_ratings m ON m.mpa_id = f.mpa_id
                LEFT JOIN (
                    SELECT film_id, COUNT(user_id) AS cnt
                    FROM likes
                    GROUP BY film_id
                ) l ON l.film_id = f.film_id
                WHERE fd.director_id = ?
                ORDER BY likes_count DESC, f.film_name ASC
            """;

    private static final String SEARCH = """
            SELECT f.*, m.mpa_name, m.description AS mpa_description,
                   COALESCE(l.cnt, 0) AS likes_count
            FROM films f
            JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
            LEFT JOIN (
                SELECT film_id, COUNT(user_id) AS cnt
                FROM likes
                GROUP BY film_id
            ) l ON l.film_id = f.film_id
            WHERE (
              (? AND LOWER(f.film_name) LIKE LOWER(?))
              OR
              (? AND EXISTS (
                  SELECT 1
                  FROM film_directors fd
                  JOIN directors d ON d.director_id = fd.director_id
                  WHERE fd.film_id = f.film_id
                    AND LOWER(d.director_name) LIKE LOWER(?)
              ))
            )
            ORDER BY likes_count DESC, f.film_id ASC
            """;

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
        saveDirectors(film);
        return getById(film.getId());
    }

    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder("""
                SELECT f.*, m.mpa_name, m.description as mpa_description,
                       COUNT(l.user_id) as likes_count
                FROM films f
                JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
                LEFT JOIN likes l ON f.film_id = l.film_id
                """);

        if (genreId != null) {
            sql.append(" JOIN film_genres fg ON f.film_id = fg.film_id ");
        }

        sql.append(" WHERE 1=1 ");

        if (genreId != null) {
            sql.append(" AND fg.genre_id = ").append(genreId);
        }
        if (year != null) {
            sql.append(" AND EXTRACT(YEAR FROM f.film_release_date) = ").append(year);
        }

        sql.append("""
                GROUP BY f.film_id
                ORDER BY likes_count DESC
                LIMIT ?
                """);

        List<Film> films = jdbcTemplate.query(sql.toString(), filmRowMapper, count);
        enrichFilmsWithLikesAndGenres(films);
        return films;
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
        updateDirectors(film);
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
        loadDirectors(film);
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
        try {
            jdbcTemplate.update(sql, filmId, userId);
        } catch (DuplicateKeyException e) {
            log.debug("Duplicate like: user {} already liked film {}", userId, filmId);
            throw e; // Пробрасываем исключение для обработки в сервисе
        }
    }

    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, filmId, userId);
            if (rowsAffected == 0) {
                log.debug("Like not found: user {} didn't like film {}", userId, filmId);
                throw new NotFoundException("Like not found");
            }
        } catch (Exception e) {
            log.debug("Error removing like: user {}, film {}", userId, filmId);
            throw e;
        }
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

    public List<Film> getRecommendedFilms(int id) {
        List<Film> films = jdbcTemplate.query(GET_RECOMMENDED_FILMS_SQL, filmRowMapper, id, id);
        enrichFilmsWithLikesAndGenres(films);
        return films;
    }

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
            int filmId = rs.getInt("film_id");
            genresByFilm
                    .computeIfAbsent(filmId, k -> new LinkedHashSet<>())
                    .add(new Genre(rs.getInt("genre_id"), rs.getString("genre_name")));
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
        Map<Integer, Set<Director>> directorsByFilm = loadDirectorsForFilms(filmIds);

        films.forEach(film -> {
            film.setLikes(likesByFilm.getOrDefault(film.getId(), new HashSet<>()));
            film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setDirectors(directorsByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));
        });
    }

    private void updateDirectors(Film film) {
        jdbcTemplate.update(DELETE_DIRECTORS, film.getId());
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) return;

        var batch = film.getDirectors().stream()
                .map(d -> new Object[]{film.getId(), d.getId()})
                .toList();
        jdbcTemplate.batchUpdate(INSERT_DIRECTOR, batch);

    }

    private void saveDirectors(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) return;
        var batch = film.getDirectors().stream()
                .map(d -> new Object[]{film.getId(), d.getId()})
                .toList();
        jdbcTemplate.batchUpdate(INSERT_DIRECTOR, batch);
    }

    private void loadDirectors(Film film) {
        String sql = """
                SELECT d.director_id, d.director_name
                FROM film_directors fd
                JOIN directors d ON d.director_id = fd.director_id
                WHERE fd.film_id = ?
                ORDER BY d.director_id
                """;
        List<Director> list = jdbcTemplate.query(sql, (rs, rn) ->
                Director.builder()
                        .id(rs.getInt("director_id"))
                        .name(rs.getString("director_name"))
                        .build(), film.getId());
        film.setDirectors(new LinkedHashSet<>(list));
    }

    @Override
    public List<Film> getFilmsDerectorByDate(int id) {
        List<Film> films = jdbcTemplate.query(GET_BY_DIRECTOR_ORDER_BY_DATE, filmRowMapper, id);
        enrichFilmsWithLikesAndGenres(films);
        return films;
    }

    @Override
    public List<Film> getFilmsDerectorByLike(int id) {
        List<Film> films = jdbcTemplate.query(GET_BY_DIRECTOR_ORDER_BY_LIKES, filmRowMapper, id);
        enrichFilmsWithLikesAndGenres(films);
        return films;
    }

    private Map<Integer, Set<Director>> loadDirectorsForFilms(List<Integer> filmIds) {
        if (filmIds.isEmpty()) Map.of();

        String sql = """
                SELECT fd.film_id, d.director_id, d.director_name
                FROM film_directors fd
                JOIN directors d ON d.director_id = fd.director_id
                WHERE fd.film_id IN (%s)
                ORDER BY d.director_id
                """.formatted(filmIds.stream().map(id -> "?").collect(Collectors.joining(",")));

        Map<Integer, Set<Director>> result = new HashMap<>();

        jdbcTemplate.query(sql, ps -> {
            for (int i = 0; i < filmIds.size(); i++) ps.setInt(i + 1, filmIds.get(i));
        }, rs -> {
            int filmId = rs.getInt("film_id");
            result.computeIfAbsent(filmId, k -> new LinkedHashSet<>())
                    .add(Director.builder()
                            .id(rs.getInt("director_id"))
                            .name(rs.getString("director_name"))
                            .build());
        });

        return result;
    }

    @Override
    public List<Film> searchFilms(String query, boolean byTitle, boolean byDirector) {
        String pat = "%" + query + "%";

        List<Film> films = jdbcTemplate.query(
                SEARCH,
                filmRowMapper,
                byTitle, pat,
                byDirector, pat
        );

        enrichFilmsWithLikesAndGenres(films);
        Map<Integer, Set<Director>> dirs = loadDirectorsForFilms(
                films.stream().map(Film::getId).toList()
        );
        films.forEach(f -> f.setDirectors(dirs.getOrDefault(f.getId(), new LinkedHashSet<>())));
        return films;
    }
}