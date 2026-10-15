package com.eventsphere.modules.session;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class QuestionUpvoteId implements Serializable {

    @Column(name = "question_id", length = 64)
    private String questionId;

    @Column(name = "attendee_id", length = 64)
    private String attendeeId;

    public QuestionUpvoteId() {}

    public QuestionUpvoteId(String questionId, String attendeeId) {
        this.questionId = questionId;
        this.attendeeId = attendeeId;
    }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getAttendeeId() { return attendeeId; }
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionUpvoteId that = (QuestionUpvoteId) o;
        return Objects.equals(questionId, that.questionId) && Objects.equals(attendeeId, that.attendeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, attendeeId);
    }
}
