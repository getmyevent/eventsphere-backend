package com.eventsphere.modules.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "poll_options", uniqueConstraints = {
        @UniqueConstraint(name = "uk_poll_option", columnNames = {"poll_id", "id"})
})
public class PollOptionEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "poll_id", insertable = false, updatable = false, length = 64)
    private String pollId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    @JsonIgnore
    private PollEntity poll;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private int votes = 0;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    public PollOptionEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPollId() { return pollId; }

    public PollEntity getPoll() { return poll; }
    public void setPoll(PollEntity poll) { this.poll = poll; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getVotes() { return votes; }
    public void setVotes(int votes) { this.votes = votes; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
