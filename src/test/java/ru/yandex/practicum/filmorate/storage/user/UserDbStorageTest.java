package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserMapper.class})
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    public void testCreateAndFindUser() {
        User user = User.builder()
                .email("test@example.com")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.create(user);
        User foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.getLogin()).isEqualTo("testlogin");
        assertThat(foundUser.getName()).isEqualTo("Test User");
        assertThat(foundUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    public void testUpdateUser() {
        User user = User.builder()
                .email("test@example.com")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.create(user);

        User updatedUserData = User.builder()
                .id(createdUser.getId())
                .email("updated@example.com")
                .login("updatedlogin")
                .name("Updated Name")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        User updatedUser = userStorage.update(updatedUserData);
        User foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo("Updated Name");
        assertThat(foundUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(foundUser.getLogin()).isEqualTo("updatedlogin");
        assertThat(foundUser.getBirthday()).isEqualTo(LocalDate.of(1995, 5, 5));
    }

    @Test
    public void testGetAllUsers() {
        User user1 = User.builder()
                .email("user1@example.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1991, 2, 2))
                .build();

        userStorage.create(user1);
        userStorage.create(user2);

        assertThat(userStorage.getAll()).hasSize(2);
    }

    @Test
    public void testGetById() {
        User user = User.builder()
                .email("test@example.com")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.create(user);
        User foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.getLogin()).isEqualTo("testlogin");
    }

    @Test
    public void testUserWithEmptyName() {
        User user = User.builder()
                .email("test@example.com")
                .login("testlogin")
                .name("")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.create(user);
        User foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser.getName()).isEqualTo("testlogin");
    }

    @Test
    public void testUserWithNullName() {
        User user = User.builder()
                .email("test@example.com")
                .login("testlogin")
                .name(null)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.create(user);
        User foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser.getName()).isEqualTo("testlogin");
    }
}