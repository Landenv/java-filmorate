package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorDbStorage;

    public Director createDirector(Director director) {
        if (director == null) throw new ValidationException("director is null");
        String name = normalize(director.getName());
        validateName(name);
        ensureNameUnique(name, null);
        director.setName(name);
        return directorDbStorage.createDirector(director);
    }

    public List<Director> getDirectors() {
        return directorDbStorage.getDirectors();
    }

    public Director getDirectorsById(int id) {
        if (id <= 0) throw new ValidationException("id должен быть положительным");
        Director d = directorDbStorage.getDirectorsById(id);
        if (d == null) throw new NotFoundException("director not found: " + id);
        return d;
    }

    public Director updateDirector(Director newDirector) {
        if (newDirector == null) throw new ValidationException("Должен существовать");
        if (newDirector.getId() <= 0) throw new ValidationException("id должен быть положительным");

        getDirectorsById(newDirector.getId());
        validateName(newDirector.getName());
        ensureNameUnique(newDirector.getName(), newDirector.getId());
        newDirector.setName(newDirector.getName());
        Director updated = directorDbStorage.updateDirector(newDirector);
        if (updated == null) throw new NotFoundException("director not found: " + newDirector.getId());
        return updated;
    }

    public void deleteDirector(Integer id) {
        if (id <= 0) throw new ValidationException("id должен быть > 0");
        getDirectorsById(id);
        directorDbStorage.deleteDirector(id);
    }

    private void validateName(String name) {
        if (name == null || name.isEmpty()) throw new ValidationException("имя не может быть пустым");
        if (name.length() > 255) throw new ValidationException("имя длиннее 255 символов");
    }

    private void ensureNameUnique(String name, Integer selfId) {
        boolean dup = directorDbStorage.getDirectors().stream()
                .anyMatch(d -> name.equalsIgnoreCase(d.getName())
                        && (selfId == null || d.getId() != selfId));
        if (dup) throw new ValidationException("режиссёр с таким именем уже существует");
    }

    private static String normalize(String s) {
        return s == null ? null : s.trim().replaceAll("\\s{2,}", " ");
    }
}
