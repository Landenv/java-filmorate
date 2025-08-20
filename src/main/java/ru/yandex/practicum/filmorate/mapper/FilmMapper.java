package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder()
                .id(resultSet.getInt("film_id"))
                .name(resultSet.getString("film_name"))
                .description(resultSet.getString("film_description"))
                .releaseDate(resultSet.getDate("film_release_date").toLocalDate())
                .duration(resultSet.getInt("film_duration"))
                .mpa(new MpaRating(
                        resultSet.getInt("mpa_id"),
                        resultSet.getString("mpa_name"),
                        resultSet.getString("mpa_description")
                ))
                .build();
    }
}