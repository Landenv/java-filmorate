package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.model.Film;

@Component
public class FilmMapper {

    public Film convertToFilm(FilmRequest filmRequest) {
        return Film.builder()
                .name(filmRequest.getName())
                .description(filmRequest.getDescription())
                .releaseDate(filmRequest.getReleaseDate())
                .duration(filmRequest.getDuration())
                .genres(filmRequest.getGenres())
                .mpa(filmRequest.getMpa())
                .build();
    }

    public void updateFilmFromRequest(Film film, FilmRequest filmRequest) {
        film.setName(filmRequest.getName());
        film.setDescription(filmRequest.getDescription());
        film.setReleaseDate(filmRequest.getReleaseDate());
        film.setDuration(filmRequest.getDuration());
        film.setGenres(filmRequest.getGenres());
        film.setMpa(filmRequest.getMpa());
    }
}