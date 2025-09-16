package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Repository
@Qualifier("directorDbStorage")
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbc;
    private final DirectorRowMapper directorRowMapper;

    private static final String CREATE = """
            INSERT INTO directors(director_name) VALUES (?)""";

    private static final String GET = """
            SELECT director_id as id, director_name as name
            FROM directors
            ORDER BY director_id
            """;

    private static final String GET_BY_ID = """
            SELECT director_id as id, director_name as name
            FROM directors
            WHERE director_id=?
            """;

    private static final String UPDATE = """
            UPDATE directors SET director_name = ? WHERE director_id = ?
            """;

    private static final String DELETE = """
                        DELETE FROM film_directors WHERE director_id = ?
            """;


    @Override
    public void createDirector(Director director) {

    }

    @Override
    public List<Director> getDirectors() {
        return jdbc.query(GET, directorRowMapper);
    }

    @Override
    public Director getDirectorsById(int id) {
        try {
            return jdbc.queryForObject(GET_BY_ID, directorRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Director updateDirector(Director newdirector) {
        int updatedRows = jdbc.update(UPDATE, newdirector.getName(), newdirector.getId());
        return updatedRows;
    }

    @Override
    public void deleteDirector(int id) {
        jdbc.update(DELETE, id);
    }
}
