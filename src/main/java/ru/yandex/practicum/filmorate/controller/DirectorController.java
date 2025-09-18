package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public List<Director> getDirectors() {
        return directorService.getDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorsById(@PathVariable("id") int id) {
        return directorService.getDirectorsById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Director createDirector(@RequestBody @Valid Director director) {
        return directorService.createDirector(director);
    }

    @PutMapping
    public Director updateDirector(@RequestBody final Director newDirector) {
        return directorService.updateDirector(newDirector);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable("id") Integer id) {
        directorService.deleteDirector(id);
    }

}
