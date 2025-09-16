package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Component
public interface DirectorStorage {

    void createDirector(Director director);
    List<Director> getDirectors();
    Director getDirectorsById(int id);
    Director updateDirector(Director newdirector);
    void deleteDirector(int id);
}
