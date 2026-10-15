package com.eventsphere.modules.connection;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "connections", uniqueConstraints = {
        @UniqueConstraint(name = "uk_conn_canonical_pair", columnNames = {"event_id", "user_low", "user_high"})
})
public class ConnectionEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "event_id", nullable = false, length = 32)
    private String eventId;

    @Column(name = "sender_id", nullable = false, length = 64)
    private String senderId;

    @Column(name = "receiver_id", nullable = false, length = 64)
    private String receiverId;

    @Column(nullable = false, length = 32)
    private String status = "pending";

    @Column(nullable = false, length = 32)
    private String source = "direct";

    @Column(name = "user_low", nullable = false, length = 64)
    private String userLow;

    @Column(name = "user_high", nullable = false, length = 64)
    private String userHigh;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ConnectionEntity() {}

    public ConnectionEntity(String id, String eventId, String senderId, String receiverId, String status, String source) {
        this.id = id;
        this.eventId = eventId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.source = source;
        updateCanonicalPair();
    }

    @PrePersist
    @PreUpdate
    public void updateCanonicalPair() {
        if (senderId != null && receiverId != null) {
            if (senderId.compareTo(receiverId) <= 0) {
                this.userLow = senderId;
                this.userHigh = receiverId;
            } else {
                this.userLow = receiverId;
                this.userHigh = senderId;
            }
        }
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getUserLow() { return userLow; }
    public String getUserHigh() { return userHigh; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
