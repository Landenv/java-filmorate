package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validator.MinReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class Film {
    private int id;

    @Builder.Default
    private Set<Integer> likes = new HashSet<>();

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    @MinReleaseDate
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;

    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    @NotNull(message = "Рейтинг MPA обязателен")
    private MpaRating mpa;

    @NotEmpty(message = "Должен быть хотя бы один режиссер")
    private Set<Director> directors=new HashSet<>();
}