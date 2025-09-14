package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Component
public interface DirectorStorage {

    List<Director> getDirectors();
    Director getDirectorsById(int id);
    Director updateDirector(Director newdirector);
    void createDirector(Director director);
    void deleteDirector(int id);
}
