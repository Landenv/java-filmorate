package ru.yandex.practicum.filmorate.storage.feed;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedEventMapper implements RowMapper<FeedEvent> {

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