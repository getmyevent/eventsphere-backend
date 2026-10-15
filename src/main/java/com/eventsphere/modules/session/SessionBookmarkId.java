package com.eventsphere.modules.session;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SessionBookmarkId implements Serializable {

    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "attendee_id", length = 64)
    private String attendeeId;

    public SessionBookmarkId() {}

    public SessionBookmarkId(String sessionId, String attendeeId) {
        this.sessionId = sessionId;
        this.attendeeId = attendeeId;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getAttendeeId() { return attendeeId; }
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionBookmarkId that = (SessionBookmarkId) o;
        return Objects.equals(sessionId, that.sessionId) && Objects.equals(attendeeId, that.attendeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, attendeeId);
    }
}
