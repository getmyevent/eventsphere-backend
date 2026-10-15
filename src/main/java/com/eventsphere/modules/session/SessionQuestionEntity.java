package com.eventsphere.modules.session;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "session_questions")
public class SessionQuestionEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "attendee_id", nullable = false, length = 64)
    private String attendeeId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnonymous = false;

    @Column(name = "author_name_display", nullable = false)
    private String authorNameDisplay;

    @Column(nullable = false, length = 32)
    private String status = "Submitted";

    @Column(nullable = false)
    private int upvotes = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SessionQuestionEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getAttendeeId() { return attendeeId; }
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }

    public String getAuthorNameDisplay() { return authorNameDisplay; }
    public void setAuthorNameDisplay(String authorNameDisplay) { this.authorNameDisplay = authorNameDisplay; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
