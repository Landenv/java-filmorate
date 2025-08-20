package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Component
@Qualifier("userDbStorage")
public interface UserStorage {
    User create(User user);

    User update(User user);

    User getById(int id);

    List<User> getAll();

    void delete(int id);
}