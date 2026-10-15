package com.eventsphere.modules.attendee;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "saved_people")
@IdClass(SavedPersonId.class)
public class SavedPersonEntity {

    @Id
    @Column(name = "attendee_id", length = 64)
    private String attendeeId;

    @Id
    @Column(name = "target_person_id", length = 64)
    private String targetPersonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendee_id", insertable = false, updatable = false)
    private AttendeeEntity attendee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_person_id", insertable = false, updatable = false)
    private AttendeeEntity targetPerson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SavedPersonEntity() {}

    public SavedPersonEntity(String attendeeId, String targetPersonId) {
        this.attendeeId = attendeeId;
        this.targetPersonId = targetPersonId;
        this.createdAt = Instant.now();
    }

    public String getAttendeeId() { return attendeeId; }
    public void setAttendeeId(String attendeeId) { this.attendeeId = attendeeId; }

    public String getTargetPersonId() { return targetPersonId; }
    public void setTargetPersonId(String targetPersonId) { this.targetPersonId = targetPersonId; }

    public AttendeeEntity getAttendee() { return attendee; }
    public AttendeeEntity getTargetPerson() { return targetPerson; }

    public Instant getCreatedAt() { return createdAt; }
}
