package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {

    private final JdbcTemplate jdbcTemplate;

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
        return jdbcTemplate.query(GET_FEED_BY_USER_SQL, new FeedEventMapper(), userId);
    }

    private static class FeedEventMapper implements RowMapper<FeedEvent> {
        @Override
        public FeedEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            return FeedEvent.builder()
                    .eventId(rs.getInt("event_id"))
                    .timestamp(rs.getLong("timestamp"))
                    .userId(rs.getInt("user_id"))
                    .eventType(FeedEvent.EventType.valueOf(rs.getString("event_type")))
                    .operation(FeedEvent.Operation.valueOf(rs.getString("operation")))
                    .entityId(rs.getInt("entity_id"))
                    .build();
        }
    }
}