package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Map;

@Repository
@Qualifier("directorDbStorage")
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbc;
    private final DirectorRowMapper directorRowMapper;

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
    public Director createDirector(Director director) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbc)
                .withTableName("directors")
                .usingGeneratedKeyColumns("director_id");
        Number key = insert.executeAndReturnKey(Map.of("director_name", director.getName()));
        director.setId(key.intValue());
        return director;
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
//        int updatedRows = jdbc.update(UPDATE, newdirector.getName(), newdirector.getId());
//        return updatedRows>0? getDirectorsById(newdirector.getId()) : null;
        int n = jdbc.update(UPDATE, newdirector.getName(), newdirector.getId()); // n = число изменённых строк
        if (n == 0) {
            throw new EmptyResultDataAccessException(1);
        }
        return getDirectorsById(newdirector.getId());
    }

    @Override
    public void deleteDirector(int id) {
        jdbc.update(DELETE, id);
    }
}
