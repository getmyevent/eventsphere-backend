package com.eventsphere.modules.exhibitor;

import com.eventsphere.modules.attendee.AttendeeEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "saved_exhibitors")
@IdClass(SavedExhibitorId.class)
public class SavedExhibitorEntity {

    @Id
    @Column(name = "attendee_id", length = 64)
    private String attendeeId;

    @Id
    @Column(name = "target_exhibitor_id", length = 64)
    private String targetExhibitorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendee_id", insertable = false, updatable = false)
    private AttendeeEntity attendee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_exhibitor_id", insertable = false, updatable = false)
    private ExhibitorEntity targetExhibitor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SavedExhibitorEntity() {}

    public SavedExhibitorEntity(String attendeeId, String targetExhibitorId) {
        this.attendeeId = attendeeId;
        this.targetExhibitorId = targetExhibitorId;
        this.createdAt = Instant.now();
    }

    public String getAttendeeId() { return attendeeId; }
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }

    public String getTargetExhibitorId() { return targetExhibitorId; }
    public void setTargetExhibitorId(String targetExhibitorId) { this.targetExhibitorId = targetExhibitorId; }

    public AttendeeEntity getAttendee() { return attendee; }
    public ExhibitorEntity getTargetExhibitor() { return targetExhibitor; }

    public Instant getCreatedAt() { return createdAt; }
}
