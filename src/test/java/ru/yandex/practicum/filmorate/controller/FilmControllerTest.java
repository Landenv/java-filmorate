package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {
    private final MockMvc mockMvc;

    @Autowired
    public FilmControllerTest(WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private static Stream<String> invalidFilmsProvider() {
        return Stream.of(
                "{}",  // Пустой объект
                "{\"name\":\"\", \"releaseDate\":\"2000-01-01\", \"duration\":120, \"mpa\":{\"id\":1}}",
                "{\"name\":\"Film\", \"releaseDate\":\"1895-12-27\", \"duration\":120, \"mpa\":{\"id\":1}}",
                "{\"name\":\"Film\", \"releaseDate\":\"2000-01-01\", \"duration\":-1, \"mpa\":{\"id\":1}}",
                "{\"name\":\"Film\", \"description\":\"" + "A".repeat(201) + "\", " +
                        "\"releaseDate\":\"2000-01-01\", \"duration\":120, \"mpa\":{\"id\":1}}",
                "{\"name\":\"Film\", \"releaseDate\":\"2000-01-01\", \"duration\":120}",
                "{\"name\":\"Film\", \"releaseDate\":\"2000-01-01\", \"duration\":120, \"mpa\":{}}",
                "{\"name\":\"Film\", \"releaseDate\":\"2000-01-01\", \"duration\":120, \"mpa\":{\"id\":99}}",
                "{\"name\":\"Film\", \"releaseDate\":\"2000-01-01\", \"duration\":120, \"mpa\":{\"id\":1}, \"genres\":[{\"id\":99}]}"
        );
    }

    @ParameterizedTest
    @MethodSource("invalidFilmsProvider")
    void shouldReturn400WhenPostInvalidFilm(String json) throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldAcceptValidFilm() throws Exception {
        String validJson = "{\"name\":\"Valid Film\", \"description\":\"Description\"," +
                " \"releaseDate\":\"2000-01-01\", \"duration\":120, \"mpa\":{\"id\":1}}";
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAcceptValidFilmWithGenres() throws Exception {
        String validJson = "{\"name\":\"Valid Film\", \"description\":\"Description\"," +
                " \"releaseDate\":\"2000-01-01\", \"duration\":120, \"mpa\":{\"id\":1}, " +
                "\"genres\":[{\"id\":1}, {\"id\":2}]}";
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk());
    }
}