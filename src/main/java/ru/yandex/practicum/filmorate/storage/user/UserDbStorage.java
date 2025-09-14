package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;

    private static final String GET_USER_BY_ID_SQL = "SELECT * FROM users WHERE user_id = ?";
    private static final String GET_ALL_USERS_SQL = "SELECT * FROM users";

    @Override
    public User create(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("user_email", user.getEmail());
        parameters.put("user_login", user.getLogin());
        parameters.put("user_name", user.getName());
        parameters.put("user_birthday", user.getBirthday());

        Number generatedId = simpleJdbcInsert.executeAndReturnKey(parameters);
        user.setId(generatedId.intValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET user_email = ?, user_login = ?, user_name = ?, user_birthday = ? WHERE user_id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }

        updateSubscriptions(user);
        return getById(user.getId());
    }

    @Override
    public User getById(int id) {
        User user = jdbcTemplate.query(GET_USER_BY_ID_SQL, userMapper, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));

        loadSubscriptions(user);
        return user;
    }

    @Override
    public List<User> getAll() {
        List<User> users = jdbcTemplate.query(GET_ALL_USERS_SQL, userMapper);
        users.forEach(this::loadSubscriptions);
        return users;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(sql, id);
    }

    private void loadSubscriptions(User user) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        jdbcTemplate.query(sql, resultSet -> {
            int friendId = resultSet.getInt("friend_id");
            user.getSubscriptions().add(friendId);
        }, user.getId());
    }

    private void updateSubscriptions(User user) {
        String deleteSql = "DELETE FROM friendships WHERE user_id = ?";
        jdbcTemplate.update(deleteSql, user.getId());

        if (!user.getSubscriptions().isEmpty()) {
            String insertSql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
            user.getSubscriptions().forEach(friendId ->
                    jdbcTemplate.update(insertSql, user.getId(), friendId)
            );
        }
    }

    public void addFriend(int userId, int friendId) {
        log.info("UserDbStorage.addFriend({}, {}) - executing SQL INSERT", userId, friendId);
        String sql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        try {
            int rows = jdbcTemplate.update(sql, userId, friendId);
            log.info("SQL INSERT successful. Rows affected: {}", rows);
        } catch (Exception e) {
            log.error("SQL INSERT failed: {}", e.getMessage());
            throw e;
        }
    }

    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        try {
            int rowsDeleted = jdbcTemplate.update(sql, userId, friendId);
            log.info("Removed friendship: {} -> {}. Rows affected: {}", userId, friendId, rowsDeleted);

            if (rowsDeleted == 0) {
                log.info("Friendship {} -> {} didn't exist, but that's OK", userId, friendId);
            }
        } catch (Exception e) {
            log.error("Error removing friendship: {}", e.getMessage());
            throw e;
        }
    }

    public List<Integer> getFriendIds(int userId) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getInt("friend_id"), userId);
    }
}