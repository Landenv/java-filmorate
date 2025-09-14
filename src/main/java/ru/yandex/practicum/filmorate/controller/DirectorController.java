package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorService directorService;

    @Autowired
    public DirectorController(final DirectorService directorService) {
    }

    @GetMapping
    public List<Director> getDirectors() {
        return directorService.getDirectors();
    }

    @GetMapping("/{id}")
    public List<Director> getDirectorsById(@PathVariable("id") int id) {
        return directorService.getDirectorsById(id);
    }

    @PostMapping
    public void createDirector(@RequestBody final Director director) {
        directorService.createDirector(director);
    }


    @PutMapping
    public Director updateDirector(@RequestBody final Director newDirector) {
        return directorService.updateDirector(newDirector);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable("id") int id) {
        directorService.deleteDirector(id);
    }

}
