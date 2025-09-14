package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Component
public class DirectorDbStorage implements DirectorStorage {
    @Override
    public List<Director> getDirectors() {
        return List.of();
    }

    @Override
    public Director getDirectorsById(int id) {
        return null;
    }

    @Override
    public Director updateDirector(Director newdirector) {
        return null;
    }

    @Override
    public void createDirector(Director director) {

    }

    @Override
    public void deleteDirector(int id) {

    }
}
