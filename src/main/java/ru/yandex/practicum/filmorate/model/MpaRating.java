package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MpaRating {
    G("G — Нет возрастных ограничений"),
    PG("PG — Детям с родителями"),
    PG_13("PG-13 — До 13 лет нежелательно"),
    R("R — До 17 лет с взрослым"),
    NC_17("NC-17 — До 18 лет запрещено");

    private final String description;
}