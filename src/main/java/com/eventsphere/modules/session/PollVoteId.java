package com.eventsphere.modules.session;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PollVoteId implements Serializable {

    @Column(name = "poll_id", length = 64)
    private String pollId;

    @Column(name = "attendee_id", length = 64)
    private String attendeeId;

    public PollVoteId() {}

    public PollVoteId(String pollId, String attendeeId) {
        this.pollId = pollId;
        this.attendeeId = attendeeId;
    }

    public String getPollId() { return pollId; }
    public void setPollId(String pollId) { this.pollId = pollId; }

    public String getAttendeeId() { return attendeeId; }
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PollVoteId that = (PollVoteId) o;
        return Objects.equals(pollId, that.pollId) && Objects.equals(attendeeId, that.attendeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pollId, attendeeId);
    }
}
