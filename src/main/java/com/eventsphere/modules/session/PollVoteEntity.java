package com.eventsphere.modules.session;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "poll_votes")
public class PollVoteEntity {

    @EmbeddedId
    private PollVoteId id;

    @Column(name = "option_id", nullable = false, length = 64)
    private String optionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public PollVoteEntity() {}

    public PollVoteEntity(String pollId, String attendeeId, String optionId) {
        this.id = new PollVoteId(pollId, attendeeId);
        this.optionId = optionId;
        this.createdAt = Instant.now();
    }

    public PollVoteId getId() { return id; }
    public void setId(PollVoteId id) { this.id = id; }

    public String getOptionId() { return optionId; }
    public void setOptionId(String optionId) { this.optionId = optionId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
