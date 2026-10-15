package com.eventsphere.modules.session;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "session_bookmarks")
public class SessionBookmarkEntity {

    @EmbeddedId
    private SessionBookmarkId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SessionBookmarkEntity() {}

    public SessionBookmarkEntity(String sessionId, String attendeeId) {
        this.id = new SessionBookmarkId(sessionId, attendeeId);
        this.createdAt = Instant.now();
    }

    public SessionBookmarkId getId() { return id; }
    public void setId(SessionBookmarkId id) { this.id = id; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
