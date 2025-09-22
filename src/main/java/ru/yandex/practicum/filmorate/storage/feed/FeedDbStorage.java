package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FeedEventMapper feedEventMapper;

    private static final String INSERT_EVENT_SQL = """
            INSERT INTO feed_events(timestamp, user_id, event_type, operation, entity_id)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String GET_FEED_BY_USER_SQL = """
            SELECT * FROM feed_events
            WHERE user_id = ?
            ORDER BY event_id ASC
            """;

    @Override
    public void addEvent(FeedEvent event) {
        jdbcTemplate.update(
                INSERT_EVENT_SQL,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId()
        );
    }

    @Override
    public List<FeedEvent> getUserFeed(int userId) {
        return jdbcTemplate.query(GET_FEED_BY_USER_SQL, feedEventMapper, userId);
    }
}