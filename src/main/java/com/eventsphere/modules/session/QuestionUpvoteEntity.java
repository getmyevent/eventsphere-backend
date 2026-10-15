package com.eventsphere.modules.session;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "question_upvotes")
public class QuestionUpvoteEntity {

    @EmbeddedId
    private QuestionUpvoteId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public QuestionUpvoteEntity() {}

    public QuestionUpvoteEntity(String questionId, String attendeeId) {
        this.id = new QuestionUpvoteId(questionId, attendeeId);
        this.createdAt = Instant.now();
    }

    public QuestionUpvoteId getId() { return id; }
    public void setId(QuestionUpvoteId id) { this.id = id; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
