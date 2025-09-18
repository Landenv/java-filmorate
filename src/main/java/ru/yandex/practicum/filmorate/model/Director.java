package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Director {
    private int id;

    @Size(max = 255, message = "имя длиннее 255 символов")
    @NotBlank(message = "Имя режиссера не может быть пустым")
    private String name;
}
