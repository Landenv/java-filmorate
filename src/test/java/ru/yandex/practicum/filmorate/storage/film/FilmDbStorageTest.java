package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class})
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    private int createUser(String email, String login, String name, LocalDate birthday) {
        var insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("user_email", email);
        parameters.put("user_login", login);
        parameters.put("user_name", name);
        parameters.put("user_birthday", birthday);

        return insert.executeAndReturnKey(parameters).intValue();
    }

    @Test
    public void testCreateAndFindFilm() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(new MpaRating(1, null, null))
                .build();

        Film createdFilm = filmStorage.create(film);
        Optional<Film> foundFilm = Optional.of(filmStorage.getById(createdFilm.getId()));

        assertThat(foundFilm)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f).hasFieldOrPropertyWithValue("id", createdFilm.getId());
                    assertThat(f).hasFieldOrPropertyWithValue("name", "Test Film");
                    assertThat(f).hasFieldOrPropertyWithValue("description", "Test Description");
                    assertThat(f).hasFieldOrPropertyWithValue("duration", 120);
                    assertThat(f.getMpa()).isNotNull();
                    assertThat(f.getMpa().getId()).isEqualTo(1);
                    assertThat(f.getMpa().getName()).isEqualTo("G");
                    assertThat(f.getMpa().getDescription()).isEqualTo("Нет возрастных ограничений");
                });
    }

    @Test
    public void testUpdateFilm() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(new MpaRating(1, null, null))
                .build();

        Film createdFilm = filmStorage.create(film);

        Film updatedFilmData = Film.builder()
                .id(createdFilm.getId())
                .name("Updated Film")
                .description("Updated Description")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(150)
                .mpa(new MpaRating(2, null, null))
                .build();

        Film updatedFilm = filmStorage.update(updatedFilmData);
        Film foundFilm = filmStorage.getById(createdFilm.getId());

        assertThat(foundFilm.getName()).isEqualTo("Updated Film");
        assertThat(foundFilm.getDescription()).isEqualTo("Updated Description");
        assertThat(foundFilm.getDuration()).isEqualTo(150);
        assertThat(foundFilm.getMpa().getId()).isEqualTo(2);
        assertThat(foundFilm.getMpa().getName()).isEqualTo("PG");
        assertThat(foundFilm.getMpa().getDescription()).isEqualTo("Детям с родителями");
    }

    @Test
    public void testCreateFilmWithGenres() {
        Film film = Film.builder()
                .name("Test Film with Genres")
                .description("Test Description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(new MpaRating(1, null, null))
                .genres(Set.of(
                        new Genre(1, null),
                        new Genre(2, null)
                ))
                .build();

        Film createdFilm = filmStorage.create(film);
        Film foundFilm = filmStorage.getById(createdFilm.getId());

        assertThat(foundFilm.getGenres()).hasSize(2);
        assertThat(foundFilm.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1, 2);

        assertThat(foundFilm.getGenres())
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Комедия", "Драма");
    }

    @Test
    public void testUpdateFilmWithGenres() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(new MpaRating(1, null, null))
                .build();

        Film createdFilm = filmStorage.create(film);

        Film updatedFilmData = Film.builder()
                .id(createdFilm.getId())
                .name("Updated Film with Genres")
                .description("Updated Description")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(150)
                .mpa(new MpaRating(2, null, null))
                .genres(Set.of(
                        new Genre(1, null),
                        new Genre(3, null)
                ))
                .build();

        Film updatedFilm = filmStorage.update(updatedFilmData);
        Film foundFilm = filmStorage.getById(createdFilm.getId());

        assertThat(foundFilm.getName()).isEqualTo("Updated Film with Genres");
        assertThat(foundFilm.getGenres()).hasSize(2);
        assertThat(foundFilm.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1, 3);
    }

    @Test
    public void testGetAllFilms() {
        Film film1 = Film.builder()
                .name("Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(new MpaRating(1, null, null))
                .build();

        Film film2 = Film.builder()
                .name("Film 2")
                .description("Description 2")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(150)
                .mpa(new MpaRating(2, null, null))
                .build();

        filmStorage.create(film1);
        filmStorage.create(film2);

        var films = filmStorage.getAll();
        assertThat(films).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    public void testFilmLikes() {
        int userId1 = createUser("user1@test.com", "user1", "User One", LocalDate.of(1990, 1, 1));
        int userId2 = createUser("user2@test.com", "user2", "User Two", LocalDate.of(1995, 1, 1));

        Film film = Film.builder()
                .name("Test Film for Likes")
                .description("Test Description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(new MpaRating(1, null, null))
                .build();

        Film createdFilm = filmStorage.create(film);

        filmStorage.addLike(createdFilm.getId(), userId1);
        filmStorage.addLike(createdFilm.getId(), userId2);

        Film filmWithLikes = filmStorage.getById(createdFilm.getId());
        assertThat(filmWithLikes.getLikes()).hasSize(2);
        assertThat(filmWithLikes.getLikes()).containsExactlyInAnyOrder(userId1, userId2);

        filmStorage.removeLike(createdFilm.getId(), userId1);
        Film filmAfterRemove = filmStorage.getById(createdFilm.getId());
        assertThat(filmAfterRemove.getLikes()).hasSize(1);
        assertThat(filmAfterRemove.getLikes()).containsExactly(userId2);
    }
}